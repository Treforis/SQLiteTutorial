package ca.uottawa.seg2505.sqlitetutorial;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Écran de démonstration : chaque bouton appelle une opération CRUD de FeedReaderDbHelper.
 * Les accès à la base se font sur un thread d'arrière-plan, comme le recommande le tutoriel
 * (getWritableDatabase() / getReadableDatabase() peuvent être longs).
 */
public class MainActivity extends AppCompatActivity {

    private FeedReaderDbHelper dbHelper;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private EditText editTitle;
    private EditText editSubtitle;
    private EditText editNewTitle;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new FeedReaderDbHelper(getApplicationContext());

        editTitle = findViewById(R.id.editTitle);
        editSubtitle = findViewById(R.id.editSubtitle);
        editNewTitle = findViewById(R.id.editNewTitle);
        textResult = findViewById(R.id.textResult);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnFind = findViewById(R.id.btnFind);
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnList = findViewById(R.id.btnList);

        btnAdd.setOnClickListener(v -> newEntry());
        btnFind.setOnClickListener(v -> lookupEntry());
        btnUpdate.setOnClickListener(v -> updateEntry());
        btnDelete.setOnClickListener(v -> deleteEntry());
        btnList.setOnClickListener(v -> listEntries());
    }

    private String text(EditText e) {
        return e.getText().toString().trim();
    }

    private void newEntry() {
        String title = text(editTitle);
        String subtitle = text(editSubtitle);
        if (title.isEmpty()) { textResult.setText("Saisir un titre."); return; }
        runDb(() -> {
            long id = dbHelper.insertEntry(title, subtitle);
            return id == -1 ? "Erreur d'insertion." : "Ligne insérée, _id = " + id;
        });
    }

    private void lookupEntry() {
        String title = text(editTitle);
        runDb(() -> {
            List<Long> ids = dbHelper.findIdsByTitle(title);
            return ids.isEmpty() ? "Aucune ligne avec le titre « " + title + " »."
                                 : "Titre « " + title + " » trouvé, _id : " + ids;
        });
    }

    private void updateEntry() {
        String oldTitle = text(editTitle);
        String newTitle = text(editNewTitle);
        if (newTitle.isEmpty()) { textResult.setText("Saisir le nouveau titre."); return; }
        runDb(() -> dbHelper.updateTitle(oldTitle, newTitle) + " ligne(s) modifiée(s).");
    }

    private void deleteEntry() {
        String title = text(editTitle);
        runDb(() -> dbHelper.deleteByTitle(title) + " ligne(s) supprimée(s).");
    }

    private void listEntries() {
        runDb(() -> {
            List<String> rows = dbHelper.getAllEntries();
            return rows.isEmpty() ? "Table vide."
                                  : "_id | title | subtitle\n" + TextUtils.join("\n", rows);
        });
    }

    /** Exécute l'opération en arrière-plan puis affiche le résultat sur le thread UI. */
    private void runDb(Supplier<String> op) {
        dbExecutor.execute(() -> {
            String result = op.get();
            runOnUiThread(() -> textResult.setText(result));
        });
    }

    @Override
    protected void onDestroy() {
        dbExecutor.shutdown();
        dbHelper.close();
        super.onDestroy();
    }
}
