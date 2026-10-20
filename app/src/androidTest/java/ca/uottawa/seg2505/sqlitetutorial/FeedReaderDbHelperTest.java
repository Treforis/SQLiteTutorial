package ca.uottawa.seg2505.sqlitetutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

/**
 * Tests instrumentés (tutoriel « Tester et déboguer votre base de données »,
 * section « Test on a device that runs Android »), adaptés de Room à SQLiteOpenHelper :
 * chaque test utilise une base EN MÉMOIRE, recréée à vide.
 */
@RunWith(AndroidJUnit4.class)
public class FeedReaderDbHelperTest {

    private FeedReaderDbHelper dbHelper;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new FeedReaderDbHelper(context, null); // null => base en mémoire
    }

    @After
    public void closeDb() {
        dbHelper.close();
    }

    @Test
    public void writeEntryAndReadById() {
        long id = dbHelper.insertEntry("george", "premier");
        assertNotEquals(-1, id);

        List<Long> ids = dbHelper.findIdsByTitle("george");
        assertEquals(1, ids.size());
        assertEquals(Long.valueOf(id), ids.get(0));
    }

    @Test
    public void readIsSortedBySubtitleDesc() {
        long a = dbHelper.insertEntry("titre", "a");
        long c = dbHelper.insertEntry("titre", "c");
        long b = dbHelper.insertEntry("titre", "b");

        List<Long> ids = dbHelper.findIdsByTitle("titre");
        assertEquals(Arrays.asList(c, b, a), ids);
    }

    @Test
    public void updateChangesTitle() {
        dbHelper.insertEntry("MyOldTitle", "x");
        assertEquals(1, dbHelper.updateTitle("MyOldTitle", "MyNewTitle"));
        assertTrue(dbHelper.findIdsByTitle("MyOldTitle").isEmpty());
        assertEquals(1, dbHelper.findIdsByTitle("MyNewTitle").size());
    }

    @Test
    public void deleteRemovesRows() {
        dbHelper.insertEntry("MyTitle", "1");
        dbHelper.insertEntry("MyTitle", "2");
        dbHelper.insertEntry("Autre", "3");

        assertEquals(2, dbHelper.deleteByTitle("MyTitle"));
        assertEquals(1, dbHelper.getAllEntries().size());
    }

    @Test
    public void upgradeRecreatesEmptyTable() {
        dbHelper.insertEntry("cache", "sera effacé");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 1, 2);
        assertTrue(dbHelper.getAllEntries().isEmpty());
    }
}
