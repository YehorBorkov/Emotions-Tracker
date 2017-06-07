package com.egorb.emotionstracker.service;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by egorb on 06-06-2017.
 */

public class PermissionChecker {

    public static final int STORAGE_PERMISSION_REQUEST = 656;
    public static final int LOCATION_PERMISSION_REQUEST = 945;

    public static boolean checkStoragePermissions(Context context) {
//        boolean MANAGE_DOCUMENTS = ActivityCompat.checkSelfPermission(context, Manifest.permission.MANAGE_DOCUMENTS) == PackageManager.PERMISSION_GRANTED;
        boolean READ_EXTERNAL_STORAGE = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean WRITE_EXTERNAL_STORAGE = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        Log.i("PermissionChecker", "MANAGE_DOCUMENTS IS " + String.valueOf(MANAGE_DOCUMENTS));
        Log.i("PermissionChecker", "READ_EXTERNAL_STORAGE IS " + String.valueOf(READ_EXTERNAL_STORAGE));
        Log.i("PermissionChecker", "WRITE_EXTERNAL_STORAGE IS " + String.valueOf(WRITE_EXTERNAL_STORAGE));
//        if (!(MANAGE_DOCUMENTS && READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE)) {
        if (!(READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(context, "Please grant permissions via settings screen", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean checkNetworkingPermissions(Context context) {
        boolean INTERNET = ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        Log.i("PermissionChecker", "INTERNET IS " + String.valueOf(INTERNET));
        boolean ACCESS_FINE_LOCATION = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!(INTERNET && ACCESS_FINE_LOCATION)) {
            Toast.makeText(context, "Please grant permissions via settings screen", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
