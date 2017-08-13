package com.example.android.inventoryapp;

/**
 * Created by HiirMii on 2017-07-10.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.DiscContract.DiscEntry;

/**
 * {@link DiscCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of disc data in the {@link Cursor}.
 */
public class DiscCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link DiscCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public DiscCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the disc data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the artist for the current disc can be set on the
     * list_item_artist TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        ImageView discImageView = (ImageView) view.findViewById(R.id.list_item_image);
        TextView artistTextView = (TextView) view.findViewById(R.id.list_item_artist);
        TextView titleTextView = (TextView) view.findViewById(R.id.list_item_title);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price_value);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity_value);

        // Find the columns of disc attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(DiscEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_IMAGE);
        int artistColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_ARTIST);
        int titleColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_TITLE);
        int priceColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(DiscEntry.COLUMN_DISC_QUANTITY);

        // Read the disc attributes from the Cursor for the current disc
        final int discId = cursor.getInt(idColumnIndex);
        Uri discImage = Uri.parse(cursor.getString(imageColumnIndex));
        String discArtist = cursor.getString(artistColumnIndex);
        String discTitle = cursor.getString(titleColumnIndex);
        int discPrice = cursor.getInt(priceColumnIndex);
        int discQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current disc
        discImageView.setImageURI(discImage);
        artistTextView.setText(discArtist);
        titleTextView.setText(discTitle);
        priceTextView.setText(Integer.toString(discPrice));
        quantityTextView.setText(Integer.toString(discQuantity));

        // Hide Sell Button if currentQuantity is zero
        Button sellButton = (Button) view.findViewById(R.id.list_item_sell_button);
        if (discQuantity == 0) {
            sellButton.setVisibility(View.GONE);
        } else {
            sellButton.setVisibility(View.VISIBLE);
        }

        // Decrease currentQuantity of the current disc when sell button is clicked
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the current currentQuantity for item
                int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
                // decrease the currentQuantity by 1
                currentQuantity -= 1;
                // display the changed currentQuantity value
                quantityTextView.setText(Integer.toString(currentQuantity));
                // store changed currentQuantity in the database
                ContentValues values = new ContentValues();
                values.put(DiscEntry.COLUMN_DISC_QUANTITY, currentQuantity);
                Uri currentDiscUri = ContentUris.withAppendedId(DiscEntry.CONTENT_URI, discId);
                // Check if database was successfully updated
                int rowsAffected = context.getContentResolver().update(currentDiscUri, values, null, null);
                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    Toast.makeText(context.getApplicationContext(), "Error with currentQuantity update.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context.getApplicationContext(), "Quantity successfully updated.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
