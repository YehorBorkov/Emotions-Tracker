package com.egorb.emotionstracker.service;

import android.content.Context;
import android.widget.ImageView;

import com.egorb.emotionstracker.R;

/**
 * Created by egorb on 04-06-2017.
 */

public final class ImageProvider {

    public static void setImage(Context context, ImageView view, String imageData) {
        int rating;
        try {
            double rate = Integer.parseInt(imageData);
            rating = (int) Math.ceil(rate/10);
        } catch (NumberFormatException e) {
            rating = -1;
        }
        switch (rating) {
            case 1:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_10));
                break;
            case 2:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_20));
                break;
            case 3:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_30));
                break;
            case 4:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_40));
                break;
            case 5:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_50));
                break;
            case 6:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_60));
                break;
            case 7:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_70));
                break;
            case 8:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_80));
                break;
            case 9:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_90));
                break;
            case 10:
                view.setImageDrawable(context.getDrawable(R.drawable.ic_emotion_face_100));
                break;
            default:
                view.setImageDrawable(context.getDrawable(R.drawable.smiley_smile_2));
        }
    }

}
