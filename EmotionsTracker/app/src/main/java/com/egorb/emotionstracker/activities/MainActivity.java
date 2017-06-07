package com.egorb.emotionstracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;
import com.egorb.emotionstracker.service.EmotionsListAdapter;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, EmotionsListAdapter.EmotionsAdapterOnClickHandler,
        NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int EMOTIONS_LOADER_ID = 819;
    public static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private EmotionsListAdapter mAdapter;

    private FloatingActionButton mFloatingActionButton;

    private NavigationView mNavigationView;
    private TextView mUsernameTextView;

    private String mUsername = "Default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        //Setup RecyclerView
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

        //Setup FloatingActionButton
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab_go_add);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goAdd = new Intent(MainActivity.this, AddActivity.class);
                startActivity(goAdd);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nv_navigation);
        mNavigationView.setNavigationItemSelectedListener(this);
        mUsernameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.navig_header_username);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = sharedPreferences.getString(getResources().getString(R.string.username_key),
                getResources().getString(R.string.default_username));
        String finalUserText = getString(R.string.current_user_prefix) + " " + mUsername;
        mUsernameTextView.setText(finalUserText);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        getSupportLoaderManager().initLoader(EMOTIONS_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(EMOTIONS_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
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
                String selection = EmotionsContract.EmotionsEntry.COLUMN_USERNAME + "=?";
                String[] selectionArgs = new String[] {mUsername};
                try {
                    return getContentResolver().query(EmotionsContract.EmotionsEntry.CONTENT_URI,
                            null,
                            selection,
                            selectionArgs,
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_stats:
                break;
            case R.id.nav_settings:
                Intent openSettings = new Intent(this, SettingsActivity.class);
                startActivity(openSettings);
                break;
            case R.id.nav_about:
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.username_key))) {
            mUsername = sharedPreferences.getString(key, getResources().getString(R.string.default_username));
            String finalUserText = getString(R.string.current_user_prefix) + " " + mUsername;
            mUsernameTextView.setText(finalUserText);
            getSupportLoaderManager().restartLoader(EMOTIONS_LOADER_ID, null, this);
        }
    }
}
