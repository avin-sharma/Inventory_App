package com.avinsharma.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.avinsharma.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements
        android.app.LoaderManager.LoaderCallbacks<Cursor> {

    final String LOG_TAG = EditorActivity.class.getSimpleName();
    private Uri mProductUri;
    final int PRODUCT_LOADER_ID = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;

    EditText productName;
    EditText productPrice;
    EditText productQuantity;
    EditText variableQuantity;
    EditText productEmail;
    Button sale;
    Button receive;
    Button order;
    Button addImage;
    ImageView productImage;
    Bitmap bitmap;

    private boolean mProductHasChanged = false;
    private boolean mImageHasUploaded = false;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mProductUri = getIntent().getData();
        changeLayoutOnSource();


        productEmail = (EditText) findViewById(R.id.email_edit_text);
        productName = (EditText) findViewById(R.id.name_edit_text);
        productPrice = (EditText) findViewById(R.id.price_edit_text);
        productQuantity = (EditText) findViewById(R.id.quantity_edit_text);
        variableQuantity = (EditText) findViewById(R.id.variable_quantity_edit_text);
        sale = (Button) findViewById(R.id.sale_button);
        receive = (Button) findViewById(R.id.receive_button);
        order = (Button) findViewById(R.id.order_supplier_button);
        addImage = (Button) findViewById(R.id.add_image_button);
        productImage = (ImageView) findViewById(R.id.product_photo_image_view);

        productName.setOnTouchListener(mTouchListener);
        productPrice.setOnTouchListener(mTouchListener);
        productQuantity.setOnTouchListener(mTouchListener);
        variableQuantity.setOnTouchListener(mTouchListener);
        productEmail.setOnTouchListener(mTouchListener);

        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantity = productQuantity.getText().toString();
                String vQuantity = variableQuantity.getText().toString();
                if (TextUtils.isEmpty(quantity) || TextUtils.isEmpty(vQuantity)) {
                    Toast.makeText(EditorActivity.this, "Quantities should not be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    int intQuantity = Integer.parseInt(quantity);
                    int intVariableQuantity = Integer.parseInt(vQuantity);
                    int total = intQuantity + intVariableQuantity;
                    productQuantity.setText(String.valueOf(total));
                }
            }
        });

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantity = productQuantity.getText().toString();
                String vQuantity = variableQuantity.getText().toString();
                if (TextUtils.isEmpty(quantity) || TextUtils.isEmpty(vQuantity)) {
                    Toast.makeText(EditorActivity.this, "Quantities should not be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    int intQuantity = Integer.parseInt(quantity);
                    int intVariableQuantity = Integer.parseInt(vQuantity);
                    int total = intQuantity - intVariableQuantity;
                    if (total >= 0)
                        productQuantity.setText(String.valueOf(total));
                    else
                        Toast.makeText(EditorActivity.this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                }
            }

        });

        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] adresses = new String[]{productEmail.getText().toString()};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, adresses);
                startActivity(intent);
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPictureSourceOptions();
                dispatchShowPictureIntent();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mProductUri == null) {
            MenuItem item = menu.findItem(R.id.delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_product:

                if (mProductUri == null) {
                    if (checkProduct()) {
                        getContentResolver().insert(ProductEntry.CONTENT_URI, getProduct());
                        Toast.makeText(EditorActivity.this, "Product inserted!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    if (checkProduct()) {
                        getContentResolver().update(mProductUri, getProduct(), null, null);
                        Toast.makeText(EditorActivity.this, "Product updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;
            case R.id.delete:
                DialogInterface.OnClickListener discardDeleteButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getContentResolver().delete(mProductUri, null, null);
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showDeleteWarningDialog(discardDeleteButtonClickListener);
                break;

            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                }else {

                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                                }
                            };

                    showUnsavedChangesDialog(discardButtonClickListener);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkProduct() {

        boolean check = true;

        String email = productEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(EditorActivity.this, "Please enter supplier email!", Toast.LENGTH_SHORT).show();
            check = false;
            return check;
        }

        String name = productName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(EditorActivity.this, "Please enter product name!", Toast.LENGTH_SHORT).show();
            check = false;
            return check;
        }

        if (TextUtils.isEmpty(productPrice.getText().toString().trim())) {
            Toast.makeText(EditorActivity.this, "Please enter product price!", Toast.LENGTH_SHORT).show();
            check = false;
            return check;
        }

        if (TextUtils.isEmpty(productQuantity.getText().toString().trim())) {
            check = false;
            Toast.makeText(EditorActivity.this, "Please enter product quantity!", Toast.LENGTH_SHORT).show();
            return check;
        }

        if (!mImageHasUploaded) {
            check = false;
            Toast.makeText(EditorActivity.this, "Select an Image for the product!", Toast.LENGTH_SHORT).show();
        }

        return check;
    }

    private ContentValues getProduct() {

        String name = productName.getText().toString().trim();
        String email = productEmail.getText().toString().trim();
        int price = Integer.valueOf(productPrice.getText().toString().trim());
        int quantity = Integer.valueOf(productQuantity.getText().toString().trim());
        byte[] image = convertImageToBytes(((BitmapDrawable) productImage.getDrawable()).getBitmap());

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, email);
        values.put(ProductEntry.COLUMN_IMAGE, image);

        return values;
    }

    private void changeLayoutOnSource() {

        if (mProductUri == null) {
            setTitle("Insert a new Product");
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.quantity_buttons_linear_layout);
            EditText editText = (EditText) findViewById(R.id.variable_quantity_edit_text);
            Button button = (Button) findViewById(R.id.order_supplier_button);
            addImage.setText(R.string.change_image);

            linearLayout.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            button.setVisibility(View.GONE);

            invalidateOptionsMenu();
        } else {
            setTitle("Edit a Product");
            getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);
        }
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, mProductUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_NAME);
            int priceColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int quantityColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int emailColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int imageColumnIndex = data.getColumnIndex(ProductEntry.COLUMN_IMAGE);

            String name = data.getString(nameColumnIndex);
            String email = data.getString(emailColumnIndex);
            int price = data.getInt(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);


            productName.setText(name);
            productEmail.setText(email);
            productPrice.setText(String.valueOf(price));
            productQuantity.setText(String.valueOf(quantity));
            productImage.setImageResource(R.mipmap.ic_launcher);

            byte[] image = data.getBlob(imageColumnIndex);
            if (image != null)
                productImage.setImageBitmap(convertBytesToImage(image));

        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

        productName.setText("");
        productEmail.setText("");
        productPrice.setText("");
        productQuantity.setText("");
        productImage.setImageResource(R.mipmap.ic_launcher);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteWarningDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_product);
        builder.setPositiveButton(R.string.delete, discardButtonClickListener);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            NavUtils.navigateUpFromSameTask(EditorActivity.this);
        }else {

            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };

            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    private void dispatchShowPictureIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);

    }

    private void dispatchClickPictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "In onActivityResult");

        InputStream stream = null;
        switch (requestCode) {
            case REQUEST_IMAGE_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    try {

                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                        stream = getContentResolver().openInputStream(data.getData());
                        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(stream), 300, 400, true);
                        productImage.setImageBitmap(bitmap);
                        addImage.setText(R.string.change_image);
                        mProductHasChanged = true;
                        mImageHasUploaded = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {

                        if (stream != null)
                            try {
                                stream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                    }
                }
                break;
        }
    }

    // convert from bitmap to byte array
    private byte[] convertImageToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    private Bitmap convertBytesToImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
