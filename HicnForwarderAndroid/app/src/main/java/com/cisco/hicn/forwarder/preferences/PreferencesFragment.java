/*
 * Copyright (c) 2019 Cisco and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cisco.hicn.forwarder.preferences;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;

//import com.cisco.hicn.forwarder.R;

public class PreferencesFragment extends PreferenceFragmentCompat {


    private OnFragmentInteractionListener mListener;

    public PreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        setPreferencesFromResource(R.xml.root, s);

        getPreferenceScreen().findPreference(getString(R.string.enable_bonjour_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enableBonjour = (boolean) newValue;

                NativeAccess nativeAccess = NativeAccess.getInstance();

                nativeAccess.enableDiscovery(enableBonjour);
                return true;
            }
        });

        getPreferenceScreen().findPreference(getString(R.string.enable_nexthop_ipv4_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enableNextHopIPv4 = (boolean) newValue;

                NativeAccess nativeAccess = NativeAccess.getInstance();

                nativeAccess.disableIPv4(!enableNextHopIPv4);
                return true;
            }
        });

        getPreferenceScreen().findPreference(getString(R.string.enable_nexthop_ipv6_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enableNextHopIPv6 = (boolean) newValue;

                NativeAccess nativeAccess = NativeAccess.getInstance();

                nativeAccess.disableIPv6(!enableNextHopIPv6);
                return true;
            }
        });

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}



