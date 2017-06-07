package com.egorb.emotionstracker.activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;
import com.egorb.emotionstracker.service.PermissionChecker;
import com.egorb.emotionstracker.service.SelectorDialogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements SelectorDialogFragment.OnDialogOptionSelectedListener {

    private static final int MEDIA_PERMISSION_REQUEST = 174;

    Button mSelectPhoto;
    ImageView mSelectedPhoto;
    EditText mRatingEditText, mCommentEditText;
    String mCurrentPhotoPath = null;
    Uri mSelectedImageUri = null;

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

        mSelectedPhoto = (ImageView) findViewById(R.id.iv_add_image);

        ActionBar bar = getSupportActionBar();
        if (null != bar)
            bar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onDialogueOptionSelected(int action) {
        if (!PermissionChecker.checkStoragePermissions(this)) {
            return;
        }
        switch (action) {
            case SelectorDialogFragment.GALLERY_SELECTED:
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, SelectorDialogFragment.GALLERY_SELECTED);
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

    /*
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] requiredPermissions = new String[]{ Manifest.permission.MANAGE_DOCUMENTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
                requestPermissions(requiredPermissions, MEDIA_PERMISSION_REQUEST);
            }
     */

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("MMM_dd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "EmotionsTracker" + timeStamp;
        File storageDir = new File(getFilesDir(), "images");
        File image = null;
        try {
            storageDir.mkdir();
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
                    mSelectPhoto.setVisibility(View.INVISIBLE);
                    mSelectedPhoto.setImageURI(mSelectedImageUri);
                    mSelectedPhoto.setVisibility(View.VISIBLE);
                    break;
                case SelectorDialogFragment.CAMERA_SELECTED:
                    galleryAddPic();
                    mSelectPhoto.setVisibility(View.INVISIBLE);
                    mSelectedPhoto.setImageURI(mSelectedImageUri);
                    mSelectedPhoto.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            Toast.makeText(this, "Mission failed, we're gonna get him next time", Toast.LENGTH_SHORT).show();
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
        if (null != mSelectedImageUri) {
            cv.put(EmotionsContract.EmotionsEntry.COLUMN_IMAGE, mSelectedImageUri.toString());
        } else {
            cv.put(EmotionsContract.EmotionsEntry.COLUMN_IMAGE, String.valueOf(rating));
        }
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_PLACE_ID, "ChIJLfpSBfIwK0cRyqkIVNPOb3s");
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_PLACE_NAME, "Vorzel'");
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_PLACE_ADDRESS, "Vorzel', Kyivs'ka oblast, Ukraine");
        Uri uri = getContentResolver().insert(EmotionsContract.EmotionsEntry.CONTENT_URI, cv);
        if(uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
