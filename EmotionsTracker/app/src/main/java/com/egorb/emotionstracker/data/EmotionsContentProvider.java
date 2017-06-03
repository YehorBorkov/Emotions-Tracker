package com.egorb.emotionstracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by egorb on 03-06-2017.
 */

public class EmotionsContentProvider extends ContentProvider {

    private static final int EMOTIONS_DIRECTORY = 100;
    private static final int EMOTIONS_SINGLE = 101;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private EmotionsDBHelper mEmotionsDbHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(EmotionsContract.AUTHORITY, EmotionsContract.PATH_EMOTIONS, EMOTIONS_DIRECTORY);
        uriMatcher.addURI(EmotionsContract.AUTHORITY, EmotionsContract.PATH_EMOTIONS + "/#", EMOTIONS_SINGLE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context ctx = getContext();
        mEmotionsDbHelper = new EmotionsDBHelper(ctx);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mEmotionsDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor resultCursor;
        switch (match) {
            case EMOTIONS_DIRECTORY:
                resultCursor = db.query(EmotionsContract.EmotionsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case EMOTIONS_SINGLE:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id = ?";
                String[] mSelectionArgs = {id};
                resultCursor = db.query(EmotionsContract.EmotionsEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return resultCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EMOTIONS_DIRECTORY:
                return "vdn.android.cursor.dir/" + EmotionsContract.AUTHORITY + "/" + EmotionsContract.PATH_EMOTIONS;
            case EMOTIONS_SINGLE:
                return "vdn.android.cursor.item/" + EmotionsContract.AUTHORITY + "/" + EmotionsContract.PATH_EMOTIONS;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mEmotionsDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri resultUri;
        switch (match) {
            case EMOTIONS_DIRECTORY:
                long id = db.insert(EmotionsContract.EmotionsEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    resultUri = ContentUris.withAppendedId(EmotionsContract.EmotionsEntry.CONTENT_URI, id);
                }
                else {
                    throw new UnsupportedOperationException("Unable to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mEmotionsDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int deleted;
        switch (match) {
            case EMOTIONS_DIRECTORY:
                deleted = db.delete(EmotionsContract.EmotionsEntry.TABLE_NAME,
                        null,
                        null);
                break;
            case EMOTIONS_SINGLE:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id = ?";
                String[] mSelectionArgs = {id};
                deleted = db.delete(EmotionsContract.EmotionsEntry.TABLE_NAME,
                        mSelection,
                        mSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mEmotionsDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int updated;
        switch (match) {
            case EMOTIONS_SINGLE:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id = ?";
                String[] mSelectionArgs = {id};
                updated = db.update(EmotionsContract.EmotionsEntry.TABLE_NAME,
                        values,
                        mSelection,
                        mSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        return updated;
    }
}
