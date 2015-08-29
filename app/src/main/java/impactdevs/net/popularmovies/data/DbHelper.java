package impactdevs.net.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import impactdevs.net.popularmovies.data.DataContract.MovieEntry;

/**
 * Created by Ian on 8/9/2015.
 */
public class DbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "movie.db";

    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sql) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DURATION + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL );";

        Log.d("DbHelper", "onCreate (line 35): " + SQL_CREATE_MOVIE_TABLE);

        sql.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sql, int oldVersion, int newVersion) {

        Log.d("DbHelper", "Upgrading database from version " + oldVersion + " to " +
                newVersion + ". OLD DATA WILL BE DESTROYED.");

        // TODO: Currently this drops existing table if DB upgrades.
        // As the DB is going to store user favorites this likely isn't a good policy
        sql.execSQL("DROP IF TABLE EXISTS " + MovieEntry.TABLE_NAME);

        // Recreates the database
        onCreate(sql);
    }
}
