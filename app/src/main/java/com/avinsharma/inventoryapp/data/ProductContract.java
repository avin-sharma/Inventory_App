package com.avinsharma.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Avin on 01-12-2016.
 */
public final class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.avinsharma.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    public static final class ProductEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_EMAIL = "email";
        public static final String COLUMN_IMAGE = "image";
    }
}
