package com.cisco.hicn.forwarder.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;

public class CellularPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.cellular, rootKey);
    }
}
