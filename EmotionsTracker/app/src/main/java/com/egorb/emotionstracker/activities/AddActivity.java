package com.egorb.emotionstracker.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;
import com.egorb.emotionstracker.service.PermissionChecker;
import com.egorb.emotionstracker.service.SelectorDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements
        SelectorDialogFragment.OnDialogOptionSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = AddActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST_ID = 260;

    String mPlaceId = null;
    String mPlaceName = null;
    String mPlaceAddress = null;

    Button mSelectPhoto, mSelectLocation;
    ImageView mSelectedPhoto;
    EditText mRatingEditText, mCommentEditText;
    String mCurrentPhotoPath = null;
    Uri mSelectedImageUri = null;

    GoogleApiClient mGoogleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        mRatingEditText = (EditText) findViewById(R.id.et_rating);
        mCommentEditText = (EditText) findViewById(R.id.et_comment);

        mSelectPhoto = (Button) findViewById(R.id.btn_add_image);
        mSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new SelectorDialogFragment();
                dialog.show(getFragmentManager(), "Select option");
            }
        });

        final Activity mContext = this;
        mSelectLocation = (Button) findViewById(R.id.btn_add_location);
        mSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast error = Toast.makeText(mContext, "Unable to launch location selector, maybe you are offline?", Toast.LENGTH_SHORT);
                if (!PermissionChecker.checkNetworkingPermissions(getApplicationContext()))
                    return;
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    Intent placePickerIntent = builder.build(mContext);
                    startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST_ID);
                } catch (GooglePlayServicesRepairableException e) {
                    error.show();
                    Log.e(TAG, String.format("GooglePlayServices Repairable [%s]", e.getMessage()));
                } catch (GooglePlayServicesNotAvailableException e) {
                    error.show();
                    Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                } catch (Exception e) {
                    error.show();
                    Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
                }
            }
        });

        mSelectedPhoto = (ImageView) findViewById(R.id.iv_add_image);

        ActionBar bar = getSupportActionBar();
        if (null != bar)
            bar.setDisplayHomeAsUpEnabled(true);

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

    }

    @Override
    public void onDialogueOptionSelected(int action) {
        if (!PermissionChecker.checkStoragePermissions(this)) {
            return;
        }
        switch (action) {
            case SelectorDialogFragment.GALLERY_SELECTED:
                /*
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, SelectorDialogFragment.GALLERY_SELECTED);
                */
                Intent openDocIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                openDocIntent.addCategory(Intent.CATEGORY_OPENABLE);
                openDocIntent.setType("image/*");

                startActivityForResult(openDocIntent, SelectorDialogFragment.GALLERY_SELECTED);
                break;
            case SelectorDialogFragment.CAMERA_SELECTED:
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("AddActivity", "Error with file", ex);
                }
                if (photoFile != null) {
                    mSelectedImageUri = FileProvider.getUriForFile(this,
                            "com.egorb.emotionstracker.fileprovider",
                            photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
                    startActivityForResult(intent, SelectorDialogFragment.CAMERA_SELECTED);
                }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("MMM_dd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "EmotionsTracker" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            storageDir.mkdirs();
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            mCurrentPhotoPath = image.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SelectorDialogFragment.GALLERY_SELECTED:
                    mSelectedImageUri = data.getData();
                    mSelectPhoto.setVisibility(View.GONE);
                    mSelectedPhoto.setImageURI(mSelectedImageUri);
                    mSelectedPhoto.setVisibility(View.VISIBLE);
                    break;
                case SelectorDialogFragment.CAMERA_SELECTED:
                    galleryAddPic();
                    mSelectPhoto.setVisibility(View.GONE);
                    mSelectedPhoto.setImageURI(mSelectedImageUri);
                    mSelectedPhoto.setVisibility(View.VISIBLE);
                    break;
                case PLACE_PICKER_REQUEST_ID:
                    Place selectedPlace = PlacePicker.getPlace(this, data);
                    mPlaceId = selectedPlace.getId();
                    mPlaceName = (String) selectedPlace.getName();
                    mPlaceAddress = (String) selectedPlace.getAddress();
                    View placeInfoView = findViewById(R.id.location_info_wrapper);
                    TextView placeName = (TextView) placeInfoView.findViewById(R.id.place_name_text_view);
                    TextView placeAddress = (TextView) placeInfoView.findViewById(R.id.place_address_text_view);
                    placeName.setText(mPlaceName);
                    placeAddress.setText(mPlaceAddress);
                    mSelectLocation.setVisibility(View.GONE);
                    placeInfoView.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "Mission Failed, We’ll Get ’Em Next Time", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                String rating = mRatingEditText.getText().toString();
                String comment = mCommentEditText.getText().toString();
                if (!rating.equals("") && !comment.equals("")) {
                    try {
                        int ratingNum = Integer.parseInt(rating);
                        if (ratingNum < 101) {
                            addNewEmotion(ratingNum, comment);
                            finish();
                        } else {
                            throw new NumberFormatException("Value too big");
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getBaseContext(), "Please enter numeric rating value (0-100)", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Please enter values", Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewEmotion(int rating, String comment) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString(getResources().getString(R.string.username_key),
                getResources().getString(R.string.default_username));
        ContentValues cv = new ContentValues();
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_USERNAME, username);
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_RATING, rating);
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_COMMENT, comment);
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_IMAGE,
                null != mSelectedImageUri ? mSelectedImageUri.toString() : String.valueOf(rating));
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_PLACE_ID,
                null != mPlaceId ? mPlaceId : null);
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_PLACE_NAME,
                null != mPlaceName ? mPlaceName : null);
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_PLACE_ADDRESS,
                null != mPlaceAddress ? mPlaceAddress : null);
        Uri uri = getContentResolver().insert(EmotionsContract.EmotionsEntry.CONTENT_URI, cv);
        if(uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "API client connection successful!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "API client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "API client connection failed");
    }

}
