package com.cisco.hicn.forwarder.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;

public class WifiPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wifi, rootKey);

    }
}
