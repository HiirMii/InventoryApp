package com.example.android.inventoryapp.data;

/**
 * Created by HiirMii on 2017-07-08.
 */


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public class DiscContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.androidapp/discs/ is a valid path for
     * looking at pet data. content://com.example.android.androidapp/vinyl/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "vinyl".
     */
    public static final String PATH_DISCS = "discs";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DiscContract() {}

    /**
     * Inner class that defines constant values for the discs database table.
     * Each entry in the table represents a single disc.
     */
    public static final class DiscEntry implements BaseColumns {

        /** The content URI to access the disc data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_DISCS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of discs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DISCS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single disc.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +PATH_DISCS;

        /** Name of database table for discs */
        public static final String TABLE_NAME = "discs";

        /**
         * Unique ID number for the disc (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Cover image of the disc.
         *
         * Type: TEXT
         */
        public final static String COLUMN_DISC_IMAGE ="image";

        /**
         * Artist of the disc.
         *
         * Type: TEXT
         */
        public final static String COLUMN_DISC_ARTIST ="artist";

        /**
         * Title of the disc.
         *
         * Type: TEXT
         */
        public final static String COLUMN_DISC_TITLE ="title";

        /**
         * Price of the disc.
         *
         * Type: REAL
         */
        public final static String COLUMN_DISC_PRICE ="price";

        /**
         * Quantity of the disc.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_DISC_QUANTITY ="quantity";
    }
}
