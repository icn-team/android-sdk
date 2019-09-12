package com.cisco.hicn.forwarder.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.utility.Constants;

import org.apache.http.conn.util.InetAddressUtilsHC4;

public class WiredIPv4PreferencesFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences = null;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wired_ipv4, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        getPreferenceScreen().findPreference(getString(R.string.wired_source_port_ipv4_key)).setEnabled(sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv4_key), false));
        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_ipv4_key)).setEnabled(sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv4_key), false));
        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_port_ipv4_key)).setEnabled(sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv4_key), false));

        getPreferenceScreen().findPreference(getString(R.string.wired_source_port_ipv4_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int sourcePort = Integer.parseInt((String) newValue);

                if (sourcePort < 0 && sourcePort > 65535)
                    return false;
                String nextHopIp = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv4_key), getString(R.string.default_wired_nexthop_ipv4));
                int nextHopPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv4_key), getString(R.string.default_wired_nexthop_port_ipv4)));

                NativeAccess nativeAccess = NativeAccess.getInstance();

                nativeAccess.updateInterfaceIPv4(Constants.NETDEVICE_TYPE_WIRED, sourcePort, nextHopIp, nextHopPort);
                return true;
            }
        });

        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_ipv4_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                String nextHopIp = (String) newValue;
                if (!InetAddressUtilsHC4.isIPv4Address(nextHopIp)) {
                    return false;
                }

                int sourcePort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv4_key), getString(R.string.default_wired_source_port_ipv4)));
                int nextHopPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv4_key), getString(R.string.default_wired_nexthop_port_ipv4)));

                NativeAccess nativeAccess = NativeAccess.getInstance();

                nativeAccess.updateInterfaceIPv4(Constants.NETDEVICE_TYPE_WIRED, sourcePort, nextHopIp, nextHopPort);
                return true;
            }
        });

        getPreferenceScreen().findPreference(getString(R.string.wired_nexthop_port_ipv4_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int nextHopPort = Integer.parseInt((String) newValue);

                if (nextHopPort < 0 && nextHopPort > 65535)
                    return false;
                int sourcePort = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv4_key), getString(R.string.default_wired_source_port_ipv4)));

                String nextHopIp = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv4_key), getString(R.string.default_wired_nexthop_ipv4));

                NativeAccess nativeAccess = NativeAccess.getInstance();

                nativeAccess.updateInterfaceIPv4(Constants.NETDEVICE_TYPE_WIRED, sourcePort, nextHopIp, nextHopPort);
                return true;
            }
        });
    }
}
