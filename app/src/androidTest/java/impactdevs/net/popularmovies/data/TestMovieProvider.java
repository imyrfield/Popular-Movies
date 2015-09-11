/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package impactdevs.net.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import impactdevs.net.popularmovies.data.DataContract.MovieEntry;

/**
 * Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does
 * test that at least the basic functionality has been implemented correctly.
 */
public class TestMovieProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestMovieProvider.class.getSimpleName();


    /**
     * This helper function deletes all records from both database tables using the ContentProvider.
     * It also queries the ContentProvider to make sure that the database has been successfully
     * deleted, so it cannot be used until the Query and Delete functions have been written
     * in the ContentProvider.
     * <p/>
     * Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
     * the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movie table during delete",
                0, cursor.getCount());
        cursor.close();

    }

    /**
     * This helper function deletes all records from both database tables using the database
     * functions only.  This is designed to be used to reset the state of the database until the
     * delete functionality is available in the ContentProvider.
     */
//    public void deleteAllRecordsFromDB() {
//        DbHelper dbHelper = new DbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        db.delete(MovieEntry.TABLE_NAME, null, null);
//        db.close();
//    }

    /**
     * Refactor this function to use the deleteAllRecordsFromProvider functionality once
     * you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    /**
     * Since we want each test to start with a clean slate, run deleteAllRecords
     * in setUp (called by the test runner before each test).
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /**
     * This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + DataContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DataContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /**
     * This test doesn't touch the database.  It verifies that the ContentProvider returns
     * the correct type for each type of URI that it can handle.
     */
    public void testGetType() {
        // content://impactdevs.net.popularmovies/movies/
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/impactdevs.net.popularmovies/movies/
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_DIR_TYPE",
                MovieEntry.CONTENT_DIR_TYPE, type);

        long id = 1;
        // content://impactdevs.net.popularmovies/movies/1
        type = mContext.getContentResolver().getType(
                MovieEntry.buildMovieUri(id));
        // vnd.android.cursor.item/impactdevs.net.popularmovies/1
        assertEquals("Error: the MovieEntry CONTENT_URI with id should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);
    }

    /**
     * This test uses the database directly to insert and then uses the ContentProvider to
     * read out the data.  Uncomment this test to see if your location queries are
     * performing correctly.
     */
    public void testBasicMovieQueries() {
        // insert our test records into the database
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long moveRowId = db.insert(MovieEntry.TABLE_NAME, null, testValues);

        assertTrue("Unable to Insert MovieEntry into the Database", moveRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQueries, movie query", movieCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Movie Query did not properly set NotificationUri",
                    movieCursor.getNotificationUri(), MovieEntry.CONTENT_URI);
        }
    }

    /**
     * This test uses the provider to insert and then update the data. Uncomment this test to
     * see if your update location is functioning correctly.
     */
    public void testUpdateMovie() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createMovieValues();

        Uri movieUri = mContext.getContentResolver().
                insert(MovieEntry.CONTENT_URI, values);
        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieEntry._ID, movieRowId);
        updatedValues.put(MovieEntry.COLUMN_TITLE, "Santa's Village");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor movieCursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI, null,
                null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MovieEntry.CONTENT_URI, updatedValues, MovieEntry._ID + "= ?",
                new String[]{Long.toString(movieRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,   // projection
                MovieEntry._ID + " = " + movieRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateMovie.  Error validating movie entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    /**
     * Make sure we can still delete after adding/updating stuff
     * Student: Uncomment this test after you have completed writing the insert functionality
     * in your provider.  It relies on insertions with testInsertReadProvider, so insert and
     * query functionality must also be complete before this test can be used.
     */
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createMovieValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, testValues);

        // Get the joined Weather data for a specific date
        cursor = mContext.getContentResolver().query(
                MovieEntry.buildMovieUri(TestUtilities.TEST_MOVIE_ID),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location data for a specific date.",
                cursor, testValues);
    }

    /**
     * Make sure we can still delete after adding/updating stuff
     * Student: Uncomment this test after you have completed writing the delete functionality
     * in your provider.  It relies on insertions with testInsertReadProvider, so insert and
     * query functionality must also be complete before this test can be used.
     */
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, locationObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        locationObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(locationObserver);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertMovieValues(long movieRowId) {
        long currentMovieId = 1;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentMovieId+= 1 ) {
            ContentValues testValues = new ContentValues();
            testValues.put(DataContract.MovieEntry.COLUMN_MOVIE_ID, currentMovieId);
            testValues.put(DataContract.MovieEntry.COLUMN_TITLE, "What a wonderful Test");
            testValues.put(DataContract.MovieEntry.COLUMN_IMAGE_URL, "/uXZYawqUsChGSj54wcuBtEdUJbh.jpg");
            testValues.put(DataContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-06-12");
            testValues.put(DataContract.MovieEntry.COLUMN_DURATION, 120);
            testValues.put(DataContract.MovieEntry.COLUMN_RATING, 7.0);
            testValues.put(DataContract.MovieEntry.COLUMN_SYNOPSIS, "Test Movie about tests");
            Log.d("TestMovieProvider", "createBulkInsertMovieValues (line 329): " + i);
        }
        return returnContentValues;
    }
}