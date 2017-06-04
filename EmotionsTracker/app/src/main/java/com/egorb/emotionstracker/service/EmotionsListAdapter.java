package com.egorb.emotionstracker.service;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.data.EmotionsContract;

/**
 * Created by egorb on 04-06-2017.
 */

public class EmotionsListAdapter extends RecyclerView.Adapter<EmotionsListAdapter.EmotionsViewholder> {

    private Cursor mCursor;
    private Context mContext;

    public EmotionsListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public EmotionsListAdapter.EmotionsViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.main_rv_item, parent, false);
        return new EmotionsViewholder(view);
    }

    @Override
    public void onBindViewHolder(EmotionsListAdapter.EmotionsViewholder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null

        int id = mCursor.getInt(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry._ID));
        int rating = mCursor.getInt(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_RATING));
        String timestamp = mCursor.getString(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_TIMESTAMP));
        String place = mCursor.getString(mCursor.getColumnIndex(EmotionsContract.EmotionsEntry.COLUMN_PLACE_ID));

        holder.mRatingTextView.setText(String.valueOf(rating));
        holder.mRatingProgress.setProgress(rating);
        holder.mTimestampTextView.setText(timestamp);
        holder.mPlaceTextView.setText(place);

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

    class EmotionsViewholder extends RecyclerView.ViewHolder {
        TextView mRatingTextView, mTimestampTextView, mPlaceTextView;
        ProgressBar mRatingProgress;

        public EmotionsViewholder(View view) {
            super(view);
            mRatingTextView = (TextView) view.findViewById(R.id.rv_item_text_rating);
            mRatingProgress = (ProgressBar) view.findViewById(R.id.rv_item_progress_rating);
            mTimestampTextView = (TextView) view.findViewById(R.id.rv_item_timestamp);
            mPlaceTextView = (TextView) view.findViewById(R.id.rv_item_place);
        }
    }
}
