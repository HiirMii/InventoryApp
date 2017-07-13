package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.DiscContract.DiscEntry;


/**
 * Allows user to create a new CD stock position or edit an existing one.
 */
public class DetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag for the log messages */
    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    /** Identifier for the pet data loader */
    private static final int EXISTING_DISC_LOADER = 0;

    /** Content URI for the existing disc (null if it's a new disc) */
    private Uri currentUri;

    private static final int PICK_IMAGE_REQUEST = 0;

    /** ImageView field to enter the CD's cover image */
    private ImageView coverImageView;

    /** EditText field to enter the CD's artist */
    private EditText artistEditText;

    /** EditText field to enter the CD's title */
    private EditText titleEditText;

    /** EditText field to enter the CD's price */
    private EditText priceEditText;

    /** EditText field to enter the CD's quantity */
    private EditText quantityEditText;

    /** Boolean flag that keeps track of whether the disc has been edited (true) or not (false) */
    private boolean discHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the discHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            discHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new disc or editing an existing one.
        Intent intent = getIntent();
        currentUri = intent.getData();

        // If the intent DOES NOT contain a disc content URI, then we know that we are
        // creating a new disc.
        if (currentUri == null) {
            // This is a new disc, so change the app bar to say "Add to Stock"
            setTitle(getString(R.string.details_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a disc that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing disc, so change app bar to say "Edit Disc"
            setTitle(getString(R.string.details_activity_title_edit_item));

            // Initialize a loader to read the disc data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_DISC_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        coverImageView = (ImageView) findViewById(R.id.cover_image);
        artistEditText = (EditText) findViewById(R.id.edit_disc_artist);
        titleEditText = (EditText) findViewById(R.id.edit_disc_title);
        priceEditText = (EditText) findViewById(R.id.edit_disc_price);
        quantityEditText = (EditText) findViewById(R.id.edit_disc_quantity);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        coverImageView.setOnTouchListener(touchListener);
        artistEditText.setOnTouchListener(touchListener);
        titleEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
    }

    /**
     * Get user input from editor and save disc into database.
     */
    private void saveDisc () {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String artistString = artistEditText.getText().toString().trim();
        String titleString = titleEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();

        // Check if this is supposed to be a new disc
        // and check if all the fields in the editor are blank
        if (currentUri == null &&
                TextUtils.isEmpty(artistString) && TextUtils.isEmpty(titleString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)) {
            // Since no fields were modified, we can return early without creating a new disc.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and disc attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(DiscEntry.COLUMN_DISC_ARTIST, artistString);
        values.put(DiscEntry.COLUMN_DISC_TITLE, titleString);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(DiscEntry.COLUMN_DISC_PRICE, price);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(DiscEntry.COLUMN_DISC_QUANTITY, quantity);

        // Determine if this is a new or existing disc by checking if currentUri is null or not
        if (currentUri == null) {
            // This is a NEW disc, so insert a new disc into the provider,
            // returning the content URI for the new disc.
            Uri newUri = getContentResolver().insert(DiscEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_disc_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_disc_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING disc, so update the disc with content URI: currentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because currentUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(currentUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_disc_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_disc_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void pickImage(View view) {

        Intent pickImageIntent;

        if(Build.VERSION.SDK_INT < 19) {
            pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            pickImageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickImageIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        pickImageIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(pickImageIntent, "Select Image"), PICK_IMAGE_REQUEST);
        discHasChanged = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_details.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new disc, hide the "Delete" menu item.
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save disc to database
                saveDisc();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                deleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the disc hasn't changed, continue with navigating up to parent activity
                // which is the {@link StockActivity}.
                if (!discHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!discHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all disc attributes, define a projection that contains
        // all columns from the disc table
        String[] projection = {
                DiscEntry._ID,
                DiscEntry.COLUMN_DISC_ARTIST,
                DiscEntry.COLUMN_DISC_TITLE,
                DiscEntry.COLUMN_DISC_PRICE,
                DiscEntry.COLUMN_DISC_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentUri,             // Query the content URI for the current disc
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 0) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of disc attributes that we're interested in
            int artistColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_ARTIST);
            int titleColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_TITLE);
            int priceColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String artist = cursor.getString(artistColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            // Update the views on the screen with the values from the database
            artistEditText.setText(artist);
            titleEditText.setText(title);
            priceEditText.setText(Integer.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        artistEditText.setText("");
        titleEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the disc.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this disc.
     */
    private void deleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteDisc();
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
     * Perform the deletion of the disc in the database.
     */
    private void deleteDisc() {
        // Only perform the delete if this is an existing disc.
        if (currentUri != null) {
            // Call the ContentResolver to delete the disc at the given content URI.
            // Pass in null for the selection and selection args because the currentUri
            // content URI already identifies the disc that we want.
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_disc_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_disc_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
