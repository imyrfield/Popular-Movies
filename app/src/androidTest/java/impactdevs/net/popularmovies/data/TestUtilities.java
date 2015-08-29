package impactdevs.net.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import impactdevs.net.popularmovies.Utils.PollingCheck;

/**
 * Created by Ian on 8/9/2015.
 */
public class TestUtilities extends AndroidTestCase {

    static final String TEST_MOVIE = "Testing 1, 2, 3...";
    static final long TEST_MOVIE_ID =  135397;
    static final String TEST_SYNOPSIS = "Twenty-two years after the events of Jurassic " +
            "Park, Isla Nublar now features a fully functioning dinosaur theme park, Jurassic World, as originally envisioned by John Hammond.";

    static void validateCursor(String error, Cursor valueCursor,
                               ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();

    }

    static void validateCurrentRecord(String error, Cursor valueCursor,
                                      ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            if ((Cursor.FIELD_TYPE_FLOAT == valueCursor.getType(idx))
                    && valueCursor.getDouble(idx) % 1 == 0) {
                assertEquals("Value '" + entry.getValue().toString() +
                        "' did not match expected value '" + expectedValue + "'. " +
                        error, expectedValue, valueCursor.getString(idx) + ".0");
            } else {
                assertEquals("Value '" + entry.getValue().toString() +
                        "' did not match expected value '" + expectedValue + "'. " +
                        error, expectedValue, valueCursor.getString(idx));
            }
        }
    }

    static ContentValues createMovieValues() {

        ContentValues testValues = new ContentValues();
        testValues.put(DataContract.MovieEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);
        testValues.put(DataContract.MovieEntry.COLUMN_TITLE, TEST_MOVIE);
        testValues.put(DataContract.MovieEntry.COLUMN_IMAGE_URL, "/uXZYawqUsChGSj54wcuBtEdUJbh.jpg");
        testValues.put(DataContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-06-12");
        testValues.put(DataContract.MovieEntry.COLUMN_DURATION, 120);
        testValues.put(DataContract.MovieEntry.COLUMN_RATING, 7.0);
        testValues.put(DataContract.MovieEntry.COLUMN_SYNOPSIS, TEST_SYNOPSIS);

        return testValues;
    }

    static long insertMovieValues(Context c) {
        // insert our test records into the database
        DbHelper dbHelper = new DbHelper(c);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createMovieValues();

        long movieRowId;
        movieRowId = db.insert(DataContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", movieRowId != -1);

        return movieRowId;
    }

    /**
     * The functions we provide inside of TestMovieProvider use this utility class
     * to test the ContentObserver callbacks using the PollingCheck class that we
     * grabbed from the Android CTS tests. Note that this only tests that the onChange
     * function is called; it does not test that the correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
