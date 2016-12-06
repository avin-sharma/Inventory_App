package com.avinsharma.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.avinsharma.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Avin on 02-12-2016.
 */
public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    public static class ViewHolder{
        TextView name;
        TextView price;
        TextView quantity;

        public ViewHolder(View view){
            name = (TextView) view.findViewById(R.id.name_text_view);
            price = (TextView) view.findViewById(R.id.price_text_view);
            quantity = (TextView) view.findViewById(R.id.quantity_text_view);
        }
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        Button sale = (Button) view.findViewById(R.id.sale_button);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);

        final String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);

        String productPriceWithSymbol = "₹ " + String.valueOf(productPrice);

        holder.name.setText(productName);
        holder.price.setText(productPriceWithSymbol);
        holder.quantity.setText(String.valueOf(productQuantity));

        final long id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantity - 1 >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, productQuantity - 1);
                    String selection = ProductEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(id)};
                    context.getContentResolver().update(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id), values, selection, selectionArgs);
                    holder.quantity.setText(String.valueOf(productQuantity - 1));
                    Toast.makeText(context, "1 " + productName + " Sold!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "No more " + productName + " left!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
