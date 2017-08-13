package com.example.android.inventoryapp.data;

/**
 * Created by HiirMii on 2017-07-08.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.DiscContract.DiscEntry;

import static com.example.android.inventoryapp.data.DiscContract.DiscEntry.TABLE_NAME;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class DiscDBHelper extends SQLiteOpenHelper {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = DiscDBHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "stock.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link DiscDBHelper}.
     *
     * @param context of the app
     */
    public DiscDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the discs table
        String SQL_CREATE_DISCS_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + DiscEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DiscEntry.COLUMN_DISC_IMAGE + " TEXT NOT NULL, "
                + DiscEntry.COLUMN_DISC_ARTIST + " TEXT NOT NULL, "
                + DiscEntry.COLUMN_DISC_TITLE + " TEXT NOT NULL, "
                + DiscEntry.COLUMN_DISC_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + DiscEntry.COLUMN_DISC_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_DISCS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Create a String that contains the SQL statement to drop the discs table
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

        // Execute the SQL statement to delete current discs table
        db.execSQL(SQL_DELETE_ENTRIES);

        // Create new table
        onCreate(db);

    }
}
