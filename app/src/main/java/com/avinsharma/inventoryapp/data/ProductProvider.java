package com.avinsharma.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.avinsharma.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Avin on 01-12-2016.
 */
public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    public static  final int PRODUCTS = 100;
    public static  final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private ProductDbHelper mDbHelper;

    // TODO: 01-12-2016 complete all these methods
    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            case PRODUCT_ID:
                s = ProductEntry._ID + "=?";
                strings1 = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values){

        String name = values.getAsString(ProductEntry.COLUMN_NAME);
        if (name == null){
            throw new IllegalArgumentException("Product requires a name");
        }

        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
        if (price == null){
            throw new IllegalArgumentException("Product requires a price");
        }

        String email= values.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
        if (email == null){
            throw new IllegalArgumentException("Product requires a supplier email");
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, s, strings);
                break;
            case PRODUCT_ID:
                long id = ContentUris.parseId(uri);
                s = ProductEntry._ID + "=?";
                strings = new String[] {String.valueOf(id)};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, s, strings);
                break;
                default:
                    throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match){
            case PRODUCTS:
                rowsUpdated = database.update(ProductEntry.TABLE_NAME, contentValues, s, strings);
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;
            case PRODUCT_ID:
                long id = ContentUris.parseId(uri);
                s = ProductEntry._ID + "=?";
                strings = new String[] {String.valueOf(id)};
                rowsUpdated = database.update(ProductEntry.TABLE_NAME, contentValues, s, strings);
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
}
