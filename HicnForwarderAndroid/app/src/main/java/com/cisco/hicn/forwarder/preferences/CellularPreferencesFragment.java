package com.cisco.hicn.forwarder.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.utility.Constants;

public class CellularPreferencesFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.cellular, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPreferences.getBoolean(getString(R.string.enable_cellular_key), false)) {
            getPreferenceScreen().findPreference(getString(R.string.enable_cellular_key)).setSummary(getString(R.string.manual));
            getPreferenceManager().findPreference((getString(R.string.cellular_ipv4_preferences_key))).setEnabled(true);
            getPreferenceManager().findPreference((getString(R.string.cellular_ipv6_preferences_key))).setEnabled(true);
        } else {
            getPreferenceScreen().findPreference(getString(R.string.enable_cellular_key)).setSummary(getString(R.string.auto));
            getPreferenceManager().findPreference((getString(R.string.cellular_ipv4_preferences_key))).setEnabled(false);
            getPreferenceManager().findPreference((getString(R.string.cellular_ipv6_preferences_key))).setEnabled(false);
        }

        getPreferenceScreen().findPreference(getString(R.string.enable_cellular_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    getPreferenceScreen().findPreference(getString(R.string.enable_cellular_key)).setSummary(getString(R.string.manual));
                    getPreferenceManager().findPreference((getString(R.string.cellular_ipv4_preferences_key))).setEnabled(true);
                    getPreferenceManager().findPreference((getString(R.string.cellular_ipv6_preferences_key))).setEnabled(true);

                    NativeAccess nativeAccess = NativeAccess.getInstance();
                    int cellularSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv4_key), getString(R.string.default_cellular_source_port_ipv4)));
                    String cellularNextHopIPv4 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv4_key), getString(R.string.default_cellular_nexthop_ipv4));
                    int cellularNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv4_key), getString(R.string.default_cellular_nexthop_port_ipv4)));
                    nativeAccess.updateInterfaceIPv4(Constants.NETDEVICE_TYPE_CELLULAR, cellularSourcePortIPv4, cellularNextHopIPv4, cellularNextHopPortIPv4);

                    int cellularSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv6_key), getString(R.string.default_cellular_source_port_ipv6)));
                    String cellularNextHopIPv6 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv6_key), getString(R.string.default_cellular_nexthop_ipv6));
                    int cellularNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv6_key), getString(R.string.default_cellular_nexthop_port_ipv6)));
                    nativeAccess.updateInterfaceIPv6(Constants.NETDEVICE_TYPE_CELLULAR, cellularSourcePortIPv6, cellularNextHopIPv6, cellularNextHopPortIPv6);
                } else {
                    getPreferenceScreen().findPreference(getString(R.string.enable_cellular_key)).setSummary(getString(R.string.auto));
                    getPreferenceManager().findPreference((getString(R.string.cellular_ipv4_preferences_key))).setEnabled(false);
                    getPreferenceManager().findPreference((getString(R.string.cellular_ipv6_preferences_key))).setEnabled(false);

                    NativeAccess nativeAccess = NativeAccess.getInstance();
                    nativeAccess.unsetInterfaceIPv4(Constants.NETDEVICE_TYPE_CELLULAR);
                    nativeAccess.unsetInterfaceIPv6(Constants.NETDEVICE_TYPE_CELLULAR);
                }
                return true;

            }
        });
    }
}
