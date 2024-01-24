package com.jdevelopment.imagetranslator.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.jdevelopment.imagetranslator.R;

public class Settings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
