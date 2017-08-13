package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.data.DiscContract.DiscEntry;

/**
 * Created by HiirMii on 2017-07-08.
 */

/**
 * {@link ContentProvider} for Inventory app.
 */
public class DiscProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = DiscProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the discs table
     */
    private static final int DISCS = 100;

    /**
     * URI matcher code for the content URI for a single disc in the discs table
     */
    private static final int DISC_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.discs/discs" will map to the
        // integer code {@link #DISCS}. This URI is used to provide access to MULTIPLE rows
        // of the discs table.
        uriMatcher.addURI(DiscContract.CONTENT_AUTHORITY, DiscContract.PATH_DISCS, DISCS);

        // The content URI of the form "content://com.example.android.discs/discs/#" will map to the
        // integer code {@link #DISC_ID}. This URI is used to provide access to ONE single row
        // of the discs table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.discs/discs/3" matches, but
        // "content://com.example.android.discs/discs" (without a number at the end) doesn't match.
        uriMatcher.addURI(DiscContract.CONTENT_AUTHORITY, DiscContract.PATH_DISCS + "/#", DISC_ID);
    }

    /**
     * Database helper object
     */
    private DiscDBHelper discDBHelper;

    @Override
    public boolean onCreate() {
        discDBHelper = new DiscDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = discDBHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = uriMatcher.match(uri);
        switch (match) {
            case DISCS:
                // For the DISCS code, query the discs table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the discs table.
                cursor = database.query(DiscEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case DISC_ID:
                // For the DISC_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.discs/discs/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = DiscEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the discs table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(DiscEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case DISCS:
                return DiscEntry.CONTENT_LIST_TYPE;
            case DISC_ID:
                return DiscEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case DISCS:
                return insertDisc(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a disc into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertDisc(Uri uri, ContentValues values) {
        // Check that the artist field is not null
        String artist = values.getAsString(DiscEntry.COLUMN_DISC_ARTIST);
        if (artist == null) {
            throw new IllegalArgumentException("You have to provide artist for this item.");
        }
        // Check that the title field is not null
        String title = values.getAsString(DiscEntry.COLUMN_DISC_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("You have to provide title for this item.");
        }
        // Check that the price field is not null
        Integer price = values.getAsInteger(DiscEntry.COLUMN_DISC_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("You have to provide valid price for this item.");
        }

        // Check that the quantity field is not null
        Integer quantity = values.getAsInteger(DiscEntry.COLUMN_DISC_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("You have to provide valid quantity for this item.");
        }

        // Get writeable database
        SQLiteDatabase database = discDBHelper.getWritableDatabase();

        // Insert the new disc with the given values
        long id = database.insert(DiscEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the disc content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = discDBHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case DISCS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(DiscEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DISC_ID:
                // Delete a single row given by the ID in the URI
                selection = DiscEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(DiscEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case DISCS:
                return updateDisc(uri, values, selection, selectionArgs);
            case DISC_ID:
                // For the DISC_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = DiscEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDisc(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update discs in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more discs).
     * Return the number of rows that were successfully updated.
     */
    private int updateDisc(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link DiscEntry#COLUMN_DISC_ARTIST} key is present,
        // check that the artist field value is not null.
        if (values.containsKey(DiscEntry.COLUMN_DISC_ARTIST)) {
            String artist = values.getAsString(DiscEntry.COLUMN_DISC_ARTIST);
            if (artist == null) {
                throw new IllegalArgumentException("You have to provide artist for this item.");
            }
        }

        // If the {@link DiscEntry#COLUMN_DISC_TITLE} key is present,
        // check that the artist field value is not null.
        if (values.containsKey(DiscEntry.COLUMN_DISC_TITLE)) {
            String title = values.getAsString(DiscEntry.COLUMN_DISC_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("You have to provide title for this item.");
            }
        }

        // If the {@link DiscEntry#COLUMN_DISC_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(DiscEntry.COLUMN_DISC_PRICE)) {
            // Check that the price is greater than or equal to 0 $
            Integer price = values.getAsInteger(DiscEntry.COLUMN_DISC_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("You have to provide valid price for this item.");
            }
        }

        // If the {@link DiscEntry#COLUMN_DISC_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(DiscEntry.COLUMN_DISC_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(DiscEntry.COLUMN_DISC_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("You have to provide valid quantity for this item.");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = discDBHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(DiscEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
