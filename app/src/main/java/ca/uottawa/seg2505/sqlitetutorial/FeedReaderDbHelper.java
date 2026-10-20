package ca.uottawa.seg2505.sqlitetutorial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import ca.uottawa.seg2505.sqlitetutorial.FeedReaderContract.FeedEntry;

/**
 * Crée / met à jour la base et regroupe les opérations CRUD du tutoriel :
 * insert(), query(), update(), delete().
 */
public class FeedReaderDbHelper extends SQLiteOpenHelper {
    // Si le schéma change, il faut incrémenter la version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    public FeedReaderDbHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    /** name == null : base en mémoire (utilisé par les tests instrumentés). */
    FeedReaderDbHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FeedReaderContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // La base n'est qu'un cache : on supprime les données et on recommence.
        db.execSQL(FeedReaderContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // ---------- Insérer (« Put information into a database ») ----------
    /** @return l'identifiant de la nouvelle ligne, ou -1 en cas d'erreur. */
    public long insertEntry(String title, String subtitle) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);
        return db.insert(FeedEntry.TABLE_NAME, null, values);
    }

    // ---------- Lire (« Read information from a database ») ----------
    /** Identifiants des lignes dont le titre est exactement {@code title}, triés par sous-titre DESC. */
    public List<Long> findIdsByTitle(String title) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                FeedEntry.COLUMN_NAME_TITLE,
                FeedEntry.COLUMN_NAME_SUBTITLE
        };
        String selection = FeedEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { title };
        String sortOrder = FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        List<Long> itemIds = new ArrayList<>();
        try (Cursor cursor = db.query(FeedEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder)) {
            while (cursor.moveToNext()) {
                itemIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID)));
            }
        }
        return itemIds;
    }

    /** Toutes les lignes, au format « id | titre | sous-titre ». */
    public List<String> getAllEntries() {
        SQLiteDatabase db = getReadableDatabase();
        List<String> rows = new ArrayList<>();
        try (Cursor cursor = db.query(FeedEntry.TABLE_NAME, null, null, null,
                null, null, FeedEntry._ID + " ASC")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID));
                String t = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE));
                String s = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_SUBTITLE));
                rows.add(id + " | " + t + " | " + s);
            }
        }
        return rows;
    }

    // ---------- Mettre à jour (« Update a database ») ----------
    /** @return le nombre de lignes modifiées. */
    public int updateTitle(String oldTitle, String newTitle) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, newTitle);
        String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { oldTitle };
        return db.update(FeedEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    // ---------- Supprimer (« Delete information from a database ») ----------
    /** @return le nombre de lignes supprimées. */
    public int deleteByTitle(String title) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { title };
        return db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
    }
}
