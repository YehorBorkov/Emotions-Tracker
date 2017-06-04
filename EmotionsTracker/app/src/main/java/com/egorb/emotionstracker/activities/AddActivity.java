package com.egorb.emotionstracker.activities;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;

public class AddActivity extends AppCompatActivity {

    EditText mRatingEditText, mCommentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        mRatingEditText = (EditText) findViewById(R.id.et_rating);
        mCommentEditText = (EditText) findViewById(R.id.et_comment);
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
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_IMAGE, String.valueOf(rating));
        cv.put(EmotionsContract.EmotionsEntry.COLUMN_PLACE_ID, "Центр боли и страданий");
        Uri uri = getContentResolver().insert(EmotionsContract.EmotionsEntry.CONTENT_URI, cv);
        if(uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
