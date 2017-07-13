package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.DiscContract.DiscEntry;

/**
 * Displays list of CDs in stock that were entered and stored in the app.
 */
public class StockActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /** Tag for the log messages */
    public static final String LOG_TAG = StockActivity.class.getSimpleName();

    /** Identifier for the disc data loader */
    private static final int DISC_LOADER = 0;

    /** Adapter for the ListView */
    private DiscCursorAdapter discCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        // Setup FAB to open DetailsActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.stock_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StockActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the disc data
        ListView discListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        discListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of disc data in the Cursor.
        // There is no disc data yet (until the loader finishes) so pass in null for the Cursor.
        discCursorAdapter = new DiscCursorAdapter(this, null);
        discListView.setAdapter(discCursorAdapter);

        // Setup the item click listener
        discListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link DetailsActivity}
                Intent intent = new Intent(StockActivity.this, DetailsActivity.class);

                // Form the content URI that represents the specific disc that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link DiscEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.discs/discs/2"
                // if the disc with ID 2 was clicked on.
                Uri currentDiscUri = ContentUris.withAppendedId(DiscEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentDiscUri);

                // Launch the {@link DetailsActivity} to display the data for the current disc.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(DISC_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded disc data into the database. For debugging purposes only.
     */
    private void insertDisc () {

        // Create a ContentValues object where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DiscEntry.COLUMN_DISC_ARTIST, "Dream Theater");
        values.put(DiscEntry.COLUMN_DISC_TITLE, "Images and Words");
        values.put(DiscEntry.COLUMN_DISC_PRICE, 1);
        values.put(DiscEntry.COLUMN_DISC_QUANTITY, 7);

        // getting uri of the dummy_image
        /* Uri imageUri
                = Uri.parse("android.resource://com.example.android.inventoryapp/drawable/dummy_image.jpg");
        values.put(DiscEntry.COLUMN_DISC_IMAGE, String.valueOf(imageUri)); */

        // Insert a new row for dummy_data into the provider using the ContentResolver.
        // Use the {@link DiscEntry#CONTENT_URI} to indicate that we want to insert
        // into the discs database table.
        // Receive the new content URI that will allow us to access dummy data in the future.
        Uri newUri = getContentResolver().insert(DiscEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all discs in the database.
     */
    private void deleteAllDiscs() {
        int rowsDeleted = getContentResolver().delete(DiscEntry.CONTENT_URI, null, null);
        Log.v("StockActivity", rowsDeleted + " rows deleted from discs database");
    }

    private void deleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_confirmation_message);
        builder.setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllDiscs();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_stock.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_stock, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If the list is empty, hide the "Delete All Data" menu item.
        if (discCursorAdapter.getCount() == 0) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_all_entries);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDisc();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                DiscEntry._ID,
                //DiscEntry.COLUMN_DISC_IMAGE,
                DiscEntry.COLUMN_DISC_ARTIST,
                DiscEntry.COLUMN_DISC_TITLE,
                DiscEntry.COLUMN_DISC_PRICE,
                DiscEntry.COLUMN_DISC_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                DiscEntry.CONTENT_URI,  // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link DiscCursorAdapter} with this new cursor containing updated discs data
        discCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        discCursorAdapter.swapCursor(null);
    }
}
