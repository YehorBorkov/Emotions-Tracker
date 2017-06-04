package com.egorb.emotionstracker.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;

public class AddActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 630;

    Button mSelectPhoto;
    ImageView mSelectedPhoto;
    EditText mRatingEditText, mCommentEditText;
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
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

//                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                pickIntent.setType("image/*");
//
//                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
//                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

//                startActivityForResult(chooserIntent, PICK_IMAGE);

                startActivityForResult(Intent.createChooser(getIntent, "Select Picture"), PICK_IMAGE);
            }
        });

        mSelectedPhoto = (ImageView) findViewById(R.id.iv_add_image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            mSelectedImageUri = data.getData();
            mSelectPhoto.setVisibility(View.INVISIBLE);
            mSelectedPhoto.setImageURI(mSelectedImageUri);
            mSelectedPhoto.setVisibility(View.VISIBLE);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewEmotion(int rating, String comment) {
        ContentValues cv = new ContentValues();
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
