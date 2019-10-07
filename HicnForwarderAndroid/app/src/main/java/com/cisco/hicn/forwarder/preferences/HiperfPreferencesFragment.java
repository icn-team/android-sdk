package com.cisco.hicn.forwarder.preferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;

public class HiperfPreferencesFragment extends PreferenceFragmentCompat {


    private OnFragmentInteractionListener mListener;

    public HiperfPreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        setPreferencesFromResource(R.xml.hiperf, s);

        getPreferenceScreen().findPreference(getString(R.string.hiperf_window_size_key)).setEnabled(getPreferenceScreen().findPreference(getString(R.string.enable_hiperf_window_size_key)).isEnabled());


        getPreferenceScreen().findPreference(getString(R.string.hiperf_raaqm_beta_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int hiperfRaaqm = Integer.parseInt((String) newValue);

                if (hiperfRaaqm < 0 && hiperfRaaqm > 1000)
                    return false;
                return true;
            }
        });

        getPreferenceScreen().findPreference(getString(R.string.hiperf_raaqm_drop_factor_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int hiperfRaaqmDropFactor = Integer.parseInt((String) newValue);

                if (hiperfRaaqmDropFactor < 0 && hiperfRaaqmDropFactor > 100)
                    return false;
                return true;
            }
        });


        getPreferenceScreen().findPreference(getString(R.string.enable_hiperf_window_size_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enableWindowSize = (boolean) newValue;

                getPreferenceScreen().findPreference(getString(R.string.hiperf_window_size_key)).setEnabled(enableWindowSize);

                return true;
            }
        });

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}



