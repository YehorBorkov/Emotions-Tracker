package com.egorb.emotionstracker.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.egorb.emotionstracker.R;
import com.egorb.emotionstracker.service.PermissionChecker;

/**
 * Created by egorb on 07-06-2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private PreferenceScreen mPreferenceScreen;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.preference_fragment_layout);

        mSharedPreferences = getPreferenceScreen().getSharedPreferences();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mPreferenceScreen = getPreferenceScreen();

        PreferenceCategory mPermissionPreferences = (PreferenceCategory) mPreferenceScreen.findPreference("pref_key_permission_settings");
        PreferenceCategory mUserPreferences = (PreferenceCategory) mPreferenceScreen.findPreference("pref_key_user_settings");

        int count = mPermissionPreferences.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = mPermissionPreferences.getPreference(i);
            if (preference instanceof CheckBoxPreference) {
                String key = preference.getKey();
                if (key.equals(getString(R.string.media_permission_key))) {
                    if (!PermissionChecker.checkStoragePermissions(getContext())) {
                        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
                        mSharedPreferencesEditor.putBoolean(getString(R.string.media_permission_key), false);
                        mSharedPreferencesEditor.apply();
                        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                        checkBoxPreference.setChecked(false);
                        checkBoxPreference.setEnabled(true);
                        preference.setOnPreferenceChangeListener(this);
                    } else {
                        preference.setEnabled(false);
                    }
                } else if (key.equals(getString(R.string.location_permission_key))) {
                    if (!PermissionChecker.checkNetworkingPermissions(getContext())) {
                        SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
                        mSharedPreferencesEditor.putBoolean(getString(R.string.location_permission_key), false);
                        mSharedPreferencesEditor.apply();
                        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                        checkBoxPreference.setChecked(false);
                        checkBoxPreference.setEnabled(true);
                        preference.setOnPreferenceChangeListener(this);
                    } else {
                        preference.setEnabled(false);
                    }
                }
            }
        }

        count = mUserPreferences.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = mUserPreferences.getPreference(i);
            String value = mSharedPreferences.getString(preference.getKey(), getString(R.string.default_username));
            preference.setSummary(value);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                preference.setSummary(value);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String key = preference.getKey();
        if (key.equals(getString(R.string.media_permission_key))) {
            String[] permissionsInvolved = new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            requestPermissions(permissionsInvolved, PermissionChecker.STORAGE_PERMISSION_REQUEST);
            return false;
        } else if (key.equals(getString(R.string.location_permission_key))) {
            String[] permissionsInvolved = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermissions(permissionsInvolved, PermissionChecker.LOCATION_PERMISSION_REQUEST);
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionChecker.STORAGE_PERMISSION_REQUEST: {
                boolean allDone = true;
                boolean response;
                for (int result : grantResults) {
                    response = result == PackageManager.PERMISSION_GRANTED;
                    if (!response)
                        allDone = false;
                }
                if (grantResults.length > 0
                        && allDone) {
                    SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
                    mSharedPreferencesEditor.putBoolean(getString(R.string.media_permission_key), true);
                    mSharedPreferencesEditor.apply();
                    CheckBoxPreference preference = (CheckBoxPreference) findPreference(getString(R.string.media_permission_key));
                    preference.setChecked(true);
                    preference.setEnabled(false);
                } else {
                    Toast.makeText(getContext(), "Permissions not granted. Please try again.", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PermissionChecker.LOCATION_PERMISSION_REQUEST: {
                boolean allDone = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED)
                        allDone = false;
                }
                if (grantResults.length > 0
                        && allDone) {
                    SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
                    mSharedPreferencesEditor.putBoolean(getString(R.string.location_permission_key), true);
                    mSharedPreferencesEditor.apply();
                    CheckBoxPreference preference = (CheckBoxPreference) findPreference(getString(R.string.location_permission_key));
                    preference.setChecked(true);
                    preference.setEnabled(false);
                } else {
                    Toast.makeText(getContext(), "Permissions not granted. Please try again.", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
