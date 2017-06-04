package com.egorb.emotionstracker.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class EmotionsContract {
    private EmotionsContract() {}

    public static final String AUTHORITY = "com.egorb.emotionstracker";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_EMOTIONS = "emotions";


    public static final class EmotionsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EMOTIONS).build();

        public static final String TABLE_NAME = "emotions_table";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_PLACE_ID = "place_id";
        public static final String COLUMN_PLACE_NAME = "place_name";
        public static final String COLUMN_PLACE_ADDRESS = "place_address";

    }
}
