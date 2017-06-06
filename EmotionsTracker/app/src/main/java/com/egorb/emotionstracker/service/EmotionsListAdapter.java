package com.egorb.emotionstracker.service;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EmotionsListAdapter extends RecyclerView.Adapter<EmotionsListAdapter.EmotionsViewholder> {

    private EmotionsAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;
    private Context mContext;

    public interface EmotionsAdapterOnClickHandler {
        void onClick(int id);
    }

    public EmotionsListAdapter(@NonNull Context context, EmotionsAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public EmotionsListAdapter.EmotionsViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.main_rv_item, parent, false);
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        view.setMinimumWidth(displayMetrics.widthPixels);
//        Log.i("View width", String.valueOf(view.getWidth()));
        return new EmotionsViewholder(view);
    }

    @Override
    public void onBindViewHolder(EmotionsListAdapter.EmotionsViewholder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return;

        int id = mCursor.getInt(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry._ID));
        int rating = mCursor.getInt(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_RATING));
        String dateData = mCursor.getString(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_TIMESTAMP));
        Calendar date = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String resultDate;
        try {
            date.setTime(sdf.parse(dateData));
            resultDate = new SimpleDateFormat("MMM dd", Locale.ENGLISH).format(date.getTime());
        } catch (ParseException e) {
            resultDate = "Unable to fetch date data";
        }
        String comment = mCursor.getString(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_COMMENT));
        final String image = mCursor.getString(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_IMAGE));

        if (image.length() < 4) {
            ImageProvider.setImage(mContext, holder.mEmotionImageViev, image);
        } else {
            Picasso.with(mContext).load(Uri.parse(image)).into(holder.mEmotionImageViev);
        }
        holder.mRatingTextView.setText(String.valueOf(rating));
        holder.mRatingProgress.setProgress(rating);
        holder.mTimestampTextView.setText(resultDate);
        holder.mCommentTextView.setText(comment);

        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (mCursor == newCursor) {
            return null;
        }

        Cursor temp = mCursor;
        this.mCursor = newCursor;

        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    class EmotionsViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mEmotionImageViev;
        TextView mRatingTextView, mCommentTextView, mTimestampTextView;
        ProgressBar mRatingProgress;

        public EmotionsViewholder(View view) {
            super(view);
            mEmotionImageViev = (ImageView) view.findViewById(R.id.rv_item_image);
            mRatingTextView = (TextView) view.findViewById(R.id.rv_item_text_rating);
            mRatingProgress = (ProgressBar) view.findViewById(R.id.rv_item_progress_rating);
            mCommentTextView = (TextView) view.findViewById(R.id.rv_item_text_comment);
            mTimestampTextView = (TextView) view.findViewById(R.id.rv_item_text_timestamp);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int id = mCursor.getInt(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry._ID));
            mClickHandler.onClick(id);
        }

    }
}
