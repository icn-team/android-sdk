package com.cisco.hicn.forwarder.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.utility.Constants;
import com.cisco.hicn.forwarder.utility.NetdeviceTypeEnum;

public class WiredPreferencesFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wired, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPreferences.getBoolean(getString(R.string.enable_wired_key), false)) {
            getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.manual));
            getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(true);
            getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(true);
        } else {
            getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.auto));
            getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(false);
            getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(false);
        }

        getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.manual));
                    getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(true);
                    getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(true);

                    NativeAccess nativeAccess = NativeAccess.getInstance();
                    int wiredSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv4_key), getString(R.string.default_wired_source_port_ipv4)));
                    String wiredNextHopIPv4 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv4_key), getString(R.string.default_wired_nexthop_ipv4));
                    int wiredNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv4_key), getString(R.string.default_wired_nexthop_port_ipv4)));
                    nativeAccess.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv4, wiredNextHopIPv4, wiredNextHopPortIPv4);

                    int wiredSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv6_key), getString(R.string.default_wired_source_port_ipv6)));
                    String wiredNextHopIPv6 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv6_key), getString(R.string.default_wired_nexthop_ipv6));
                    int wiredNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv6_key), getString(R.string.default_wired_nexthop_port_ipv6)));
                    nativeAccess.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv6, wiredNextHopIPv6, wiredNextHopPortIPv6);
                } else {
                    getPreferenceScreen().findPreference(getString(R.string.enable_wired_key)).setSummary(getString(R.string.auto));
                    getPreferenceManager().findPreference((getString(R.string.wired_ipv4_preferences_key))).setEnabled(false);
                    getPreferenceManager().findPreference((getString(R.string.wired_ipv6_preferences_key))).setEnabled(false);

                    NativeAccess nativeAccess = NativeAccess.getInstance();
                    nativeAccess.unsetInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue());
                    nativeAccess.unsetInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue());
                }
                return true;

            }
        });
    }
}
