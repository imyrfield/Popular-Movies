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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mOpenHelper;

    static final int MOVIE = 1;
    static final int MOVIE_WITH_ID = 2;


    private static final String sMovieIdSelection =
            DataContract.MovieEntry.TABLE_NAME +
                    "." + DataContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";


    private String[] getSelectionArgs(Uri uri) {
        String[] args = new String[] {String.valueOf(ContentUris.parseId(uri))};
        return args;
    }

    /**
     * Here is where you need to create the UriMatcher.
     * #TEST by uncommenting the testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DataContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, DataContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    /**
     * Here's where you'll code the getType function that uses the UriMatcher.
     * #TEST by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return DataContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_WITH_ID:
                return DataContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {

            // "movie"
            case MOVIE:
                retCursor = db.query(
                        DataContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;

            // "movie/*"
            case MOVIE_WITH_ID:
                retCursor = db.query(
                        DataContract.MovieEntry.TABLE_NAME,
                        projection,
                        sMovieIdSelection,
                        getSelectionArgs(uri),
                        null, null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(DataContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DataContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        DataContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        DataContract.MovieEntry.TABLE_NAME + "'");
                break;
            case MOVIE_WITH_ID:
                rowsDeleted = db.delete(
                        DataContract.MovieEntry.TABLE_NAME,
                        sMovieIdSelection,
                        getSelectionArgs(uri));
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        DataContract.MovieEntry.TABLE_NAME + "'");
                break;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the actual rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated = 0;

        if (values == null) {
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch (sUriMatcher.match(uri)){

            case MOVIE:
                rowsUpdated = db.update(
                        DataContract.MovieEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;

            case MOVIE_WITH_ID:
                rowsUpdated = db.update(
                        DataContract.MovieEntry.TABLE_NAME,
                        values,
                        sMovieIdSelection,
                        getSelectionArgs(uri));
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows impacted by the update.
        return rowsUpdated;
    }

    /**
     * This method assists the testing framework in running smoothly. Read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}