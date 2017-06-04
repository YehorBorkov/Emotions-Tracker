package com.egorb.emotionstracker.activities;

import android.database.Cursor;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;
import com.egorb.emotionstracker.service.ImageProvider;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER_ID = 552;
    private Uri mUri;

    ConstraintLayout mDetailsWrapper;
    TextView mTextDetailRating;
    ProgressBar mProgressDetailProgress;
    EditText mTextDetailComment;
    ImageView mImageDetailImage;

    ProgressBar mProgressDetailLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mDetailsWrapper = (ConstraintLayout) findViewById(R.id.cl_detail_wrapper);
        mTextDetailRating = (TextView) findViewById(R.id.tv_details_rating);
        mProgressDetailProgress = (ProgressBar) findViewById(R.id.pb_details_progress);
        mTextDetailComment = (EditText) findViewById(R.id.et_details_comment);
        mImageDetailImage = (ImageView) findViewById(R.id.iv_details_image);

        mProgressDetailLoad = (ProgressBar) findViewById(R.id.pb_details_load);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mData = null;

            @Override
            protected void onStartLoading() {
                mDetailsWrapper.setVisibility(View.INVISIBLE);
                mProgressDetailLoad.setVisibility(View.VISIBLE);
                if (mData != null) {
                    deliverResult(mData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(mUri,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    Log.e("Single emotion", "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }
        if (!cursorHasValidData) {
            return;
        }

        Log.d("Tag", String.valueOf(data.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_RATING)));

        int rating = data.getInt(data.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_RATING));
        String comment = data.getString(data.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_COMMENT));
        final String image = data.getString(data.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_IMAGE));

//        int rating = 50;
//        String comment = "Some random bullshit";
//        final String image = "64";

        if (image.length() < 4) {
            ImageProvider.setImage(this, mImageDetailImage, image);
        } else {
            Picasso.Builder builder = new Picasso.Builder(this);
            builder.listener(new Picasso.Listener()
            {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
                {
                    exception.printStackTrace();
                }
            });
            builder.build().load(Uri.parse(image)).into(mImageDetailImage);
        }

        mTextDetailRating.setText(String.valueOf(rating));
        mProgressDetailProgress.setProgress(rating);
        mTextDetailComment.setText(comment);

        mDetailsWrapper.setVisibility(View.VISIBLE);
        mProgressDetailLoad.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDetailsWrapper.setVisibility(View.INVISIBLE);
        mProgressDetailLoad.setVisibility(View.VISIBLE);
    }

}
