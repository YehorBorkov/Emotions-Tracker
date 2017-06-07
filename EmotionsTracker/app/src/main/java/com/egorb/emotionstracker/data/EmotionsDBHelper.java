package com.egorb.emotionstracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by egorb on 03-06-2017.
 */

public class EmotionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "emotions.db";
    public static final int DATABASE_VERISON = 1;

    public EmotionsDBHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERISON);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_EMOTIONS_TABLE = "CREATE TABLE " +
                EmotionsContract.EmotionsEntry.TABLE_NAME + " (" +
                EmotionsContract.EmotionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EmotionsContract.EmotionsEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                EmotionsContract.EmotionsEntry.COLUMN_RATING + " INTEGER NOT NULL, " +
                EmotionsContract.EmotionsEntry.COLUMN_COMMENT + " TEXT, " +
                EmotionsContract.EmotionsEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                EmotionsContract.EmotionsEntry.COLUMN_IMAGE + " TEXT NOT NULL, " +
                EmotionsContract.EmotionsEntry.COLUMN_PLACE_ID + " TEXT, " +
                EmotionsContract.EmotionsEntry.COLUMN_PLACE_NAME + " TEXT, " +
                EmotionsContract.EmotionsEntry.COLUMN_PLACE_ADDRESS + " TEXT" +
                ");";
        db.execSQL(SQL_CREATE_EMOTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EmotionsContract.EmotionsEntry.TABLE_NAME);
        onCreate(db);
    }
}
