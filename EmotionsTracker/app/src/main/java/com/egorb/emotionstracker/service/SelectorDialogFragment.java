package com.egorb.emotionstracker.service;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class SelectorDialogFragment extends DialogFragment {

    public static final int GALLERY_SELECTED = 992;
    public static final int CAMERA_SELECTED = 396;

    private String[] options = new String[] {"From documents", "From camera"};
    private OnDialogOptionSelectedListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select action")
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mListener.onDialogueOptionSelected(GALLERY_SELECTED);
                                break;
                            case 1:
                                mListener.onDialogueOptionSelected(CAMERA_SELECTED);
                                break;
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnDialogOptionSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public interface OnDialogOptionSelectedListener {
        void onDialogueOptionSelected(int action);
    }
}
