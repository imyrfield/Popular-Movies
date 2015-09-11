package impactdevs.net.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by Ian on 8/9/2015.
 */
public class TestDbHelper extends AndroidTestCase {

    public static final String LOG_TAG = TestDbHelper.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }

    /**
     * This function gets called before each test is executed to delete the database.
     * This makes sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DataContract.MovieEntry.TABLE_NAME);

        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while ( c.moveToNext() );

        // if this fails, it means that your database doesn't contain the movie entry
        // table
        assertTrue("Error: Your database was created without the movie entry table",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DataContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for " +
                "table information.", c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(DataContract.MovieEntry._ID);
        movieColumnHashSet.add(DataContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnHashSet.add(DataContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(DataContract.MovieEntry.COLUMN_IMAGE_URL);
        movieColumnHashSet.add(DataContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(DataContract.MovieEntry.COLUMN_DURATION);
        movieColumnHashSet.add(DataContract.MovieEntry.COLUMN_RATING);
        movieColumnHashSet.add(DataContract.MovieEntry.COLUMN_SYNOPSIS);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required
        // movie entry columns
        assertTrue("Error: The database doesn't contain all the required movie entry " +
                "columns.", movieColumnHashSet.isEmpty());
        db.close();
    }

    /**
     * Here is where you will build code to test that we can insert and query the
     * movie database.  We've done a lot of work for you.  You'll want to look in TestUtilities
     * where you can uncomment out the "createNorthPoleLocationValues" function.  You can
     * also make use of the ValidateCurrentRecord function from within TestUtilities.
     */
    public void testMovieTable() {

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createMovieValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long movieRowId;
        movieRowId = db.insert(DataContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor c = db.query(
                DataContract.MovieEntry.TABLE_NAME,
                null, null, null, null, null, null);

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returned from movie query",
                c.moveToFirst());

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Movie Query Validaton Failed",
                c, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from the movie query",
                c.moveToNext());

        // Sixth Step: Close cursor and database
        c.close();
        db.close();
    }

}


