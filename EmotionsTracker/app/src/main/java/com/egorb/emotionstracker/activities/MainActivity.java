package com.egorb.emotionstracker.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;
import com.egorb.emotionstracker.service.EmotionsListAdapter;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, EmotionsListAdapter.EmotionsAdapterOnClickHandler {

    private static final int EMOTIONS_LOADER_ID = 819;
    public static final String TAG = MainActivity.class.getSimpleName();

    private EmotionsListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new EmotionsListAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int) viewHolder.itemView.getTag();
                String stringId = Integer.toString(id);
                Uri uri = EmotionsContract.EmotionsEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                getContentResolver().delete(uri, null, null);
                getSupportLoaderManager().restartLoader(EMOTIONS_LOADER_ID, null, MainActivity.this);
            }
        }).attachToRecyclerView(mRecyclerView);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab_go_add);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goAdd = new Intent(MainActivity.this, AddActivity.class);
                startActivity(goAdd);
            }
        });

        getSupportLoaderManager().initLoader(EMOTIONS_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(EMOTIONS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mData = null;

            @Override
            protected void onStartLoading() {
                if (mData != null) {
                    deliverResult(mData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
//                String[] projection =
//                        {
//                                EmotionsContract.EmotionsEntry._ID,
//                                EmotionsContract.EmotionsEntry.COLUMN_RATING,
//                                EmotionsContract.EmotionsEntry.COLUMN_COMMENT,
//                                EmotionsContract.EmotionsEntry.COLUMN_IMAGE,
//                                EmotionsContract.EmotionsEntry.COLUMN_PLACE_ID,
//                                "strftime('%m'," +
//                                        EmotionsContract.EmotionsEntry.COLUMN_TIMESTAMP +") as "
//                                        + EmotionsContract.EmotionsEntry.DATE_MONTH,
//                                "strftime('%d'," +
//                                        EmotionsContract.EmotionsEntry.COLUMN_TIMESTAMP +") as "
//                                        + EmotionsContract.EmotionsEntry.DATE_DAY
//                        };
                try {
                    return getContentResolver().query(EmotionsContract.EmotionsEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
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
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(int id) {
        Intent emotionDetailIntent = new Intent(MainActivity.this, DetailsActivity.class);
        Uri uriForDetails = EmotionsContract.EmotionsEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        emotionDetailIntent.setData(uriForDetails);
        startActivity(emotionDetailIntent);
    }
}
