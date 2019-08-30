package com.cisco.hicn.forwarder;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

//import com.cisco.hicn.forwarder.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

    private PreferenceCategory forwarderPreferenceCategory = null;
    private EditTextPreference cacheSizeEditTextPreference = null;
    private PreferenceCategory faceManagementPreferenceCategory = null;
    private ListPreference faceTypeListPreference = null;
    private ListPreference overlayDiscoveryListPreference = null;
    private PreferenceCategory overlayManualConfigurationPreferenceCategory = null;

    private OnFragmentInteractionListener mListener;

    public PreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        final PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(this.getContext());

        forwarderPreferenceCategory = new PreferenceCategory(this.getContext());
        forwarderPreferenceCategory.setOrder(0);
        forwarderPreferenceCategory.setIconSpaceReserved(false);
        forwarderPreferenceCategory.setLayoutResource(R.layout.preference_category_layout);
        forwarderPreferenceCategory.setTitle(getString(R.string.forwarder_preference_category));
        preferenceScreen.addPreference(forwarderPreferenceCategory);

        cacheSizeEditTextPreference = new EditTextPreference(this.getContext());
        cacheSizeEditTextPreference.setOrder(1);
        cacheSizeEditTextPreference.setTitle(getString(R.string.cache_size));
        cacheSizeEditTextPreference.setKey(getString(R.string.cache_size_key));
        cacheSizeEditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        cacheSizeEditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.cache_size_key), "100"));

        cacheSizeEditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.cache_size_key), "100"));
        cacheSizeEditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("aaa", preference.getSummary().toString());
                //if ((String)newValue && "".equals((String)newValue)) {

                //}
                preference.setSummary((String) newValue);
                return true;
            }
        });
        forwarderPreferenceCategory.addPreference(cacheSizeEditTextPreference);
        setPreferenceScreen(preferenceScreen);


        faceManagementPreferenceCategory = new PreferenceCategory(this.getContext());
        faceManagementPreferenceCategory.setOrder(2);
        faceManagementPreferenceCategory.setIconSpaceReserved(false);
        faceManagementPreferenceCategory.setLayoutResource(R.layout.preference_category_layout);
        faceManagementPreferenceCategory.setTitle(getString(R.string.face_management_preference_category));
        preferenceScreen.addPreference(faceManagementPreferenceCategory);
        String[] faceTypeStringArray = {getString(R.string.native_tcp), getString(R.string.native_udp), getString(R.string.overlay_tcp), getString(R.string.overlay_udp)};
        faceTypeListPreference = new ListPreference(this.getContext());
        faceTypeListPreference.setOrder(3);
        faceTypeListPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.face_type_key), getString(R.string.default_face_type)));
        faceTypeListPreference.setTitle(getString(R.string.face_type_list_preference));
        faceTypeListPreference.setDialogTitle(getString(R.string.face_type_list_preference));
        faceTypeListPreference.setPersistent(true);

        faceTypeListPreference.setEntries(faceTypeStringArray);
        faceTypeListPreference.setEntryValues(faceTypeStringArray);
        faceTypeListPreference.setValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.face_type_key), getString(R.string.default_face_type)));
        faceTypeListPreference.setKey(getString(R.string.face_type_key));
        faceTypeListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("aaa", (String) newValue);
                preference.setSummary((String) newValue);
                //preferenceScreen.removePreference(forwarderPreferenceCategory);
                return true;
            }
        });
        faceManagementPreferenceCategory.addPreference(faceTypeListPreference);

        String[] overlayDiscoveryStringArray = {getString(R.string.manual), getString(R.string.bonjour)};

        overlayDiscoveryListPreference = new ListPreference(this.getContext());
        overlayDiscoveryListPreference.setOrder(4);
        overlayDiscoveryListPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.overlay_discovery_key), getString(R.string.default_overlay_discovery)));
        overlayDiscoveryListPreference.setTitle(getString(R.string.overlay_discovery_list_preference));
        overlayDiscoveryListPreference.setDialogTitle(getString(R.string.overlay_discovery_list_preference));
        overlayDiscoveryListPreference.setPersistent(true);
        overlayDiscoveryListPreference.setEntries(overlayDiscoveryStringArray);
        overlayDiscoveryListPreference.setEntryValues(overlayDiscoveryStringArray);
        String overlayDiscoverValue = preferenceScreen.getSharedPreferences().getString(getString(R.string.overlay_discovery_key), getString(R.string.default_overlay_discovery));
        overlayDiscoveryListPreference.setValue(overlayDiscoverValue);
        if (overlayDiscoverValue.equals(getString(R.string.manual))) {
            createOverlayManualConfigurationPreferences(preferenceScreen);
        }
        overlayDiscoveryListPreference.setKey(getString(R.string.overlay_discovery_key));
        overlayDiscoveryListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                if (newValue != null && getString(R.string.manual).equals((String) newValue)) {
                    createOverlayManualConfigurationPreferences(preferenceScreen);
                } else {
                    if (overlayManualConfigurationPreferenceCategory != null)
                        faceManagementPreferenceCategory.removePreference(overlayManualConfigurationPreferenceCategory);
                }
                //preferenceScreen.removePreference(forwarderPreferenceCategory);
                return true;
            }
        });
        faceManagementPreferenceCategory.addPreference(overlayDiscoveryListPreference);

        /*ListPreference bb = new ListPreference(this.getContext());
        bb.setSummary("ciao2");
        bb.setDialogTitle("ciao2");
        bb.setPersistent(true);

        bb.setEntries(keys);
        bb.setEntryValues(keys);
        bb.setValue("bb");
        bb.setKey("aaaaaaa");
        bb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("aaa", (String)newValue);
                preference.setSummary((String)newValue);
                preferenceScreen.addPreference(forwarderPreferenceCategory);
                return true;
            }
        });
        faceManagementPreferenceCategory.addPreference(bb);*/


        /*EditTextPreference caceSizeEditTextPreference = new EditTextPreference(this.getContext());
        caceSizeEditTextPreference.setTitle(getString(R.string.cache_size));
        caceSizeEditTextPreference.setKey(getString(R.string.cache_size_key));
        caceSizeEditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        caceSizeEditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.cache_size_key), "100"));
        caceSizeEditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("aaa", preference.getSummary().toString());
                //if ((String)newValue && "".equals((String)newValue)) {

                //}
                preference.setSummary((String)newValue);
                return true;
            }
        });
        forwarderPreferenceCategory.addPreference(caceSizeEditTextPreference);*/
        setPreferenceScreen(preferenceScreen);



        /*

        screen.addPreference(category);
        EditTextPreference editTextPreference = new EditTextPreference(this.getContext());
        editTextPreference.setTitle("aaaa");
        editTextPreference.setKey("username");

        editTextPreference.setSummary(screen.getSharedPreferences().getString("username", "xxx"));
        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("aaa", preference.getSummary().toString());
                preference.setSummary((String)newValue);
                return true;
            }
        });
        //CheckBoxPreference checkBoxPref = new CheckBoxPreference(this.getContext());
        //checkBoxPref.setTitle("title");
        //checkBoxPref.setSummary("summary");
        //checkBoxPref.setChecked(true);

        category.addPreference(editTextPreference);
        setPreferenceScreen(screen);
*/
//        addPreferencesFromResource(R.xml.preferences);
    }
/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preferences, container, false);
    }*/

    private void createOverlayManualConfigurationPreferences(PreferenceScreen preferenceScreen) {

        overlayManualConfigurationPreferenceCategory = new PreferenceCategory(getContext());
        overlayManualConfigurationPreferenceCategory.setOrder(5);
        overlayManualConfigurationPreferenceCategory.setIconSpaceReserved(false);
        overlayManualConfigurationPreferenceCategory.setLayoutResource(R.layout.preference_category_layout);
        overlayManualConfigurationPreferenceCategory.setTitle(getString(R.string.overlay_manual_configuration_preference_category));
        faceManagementPreferenceCategory.addPreference(overlayManualConfigurationPreferenceCategory);

        /*
        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements(); ) {

                NetworkInterface i = list.nextElement();
                Log.i("network_interfaces", "display name " + i.getDisplayName() + " " + i.isUp());

                if (i.isUp() && !i.isLoopback()) {

                    EditTextPreference addressIpv4EditTextPreference = new EditTextPreference(getContext());
                    addressIpv4EditTextPreference.setTitle(getString(R.string.address_ipv4) + " " + i.getDisplayName());
                    addressIpv4EditTextPreference.setKey(getString(R.string.address_ipv4_key) + i.getDisplayName());
                    addressIpv4EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {

                            //editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });
                    addressIpv4EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.address_ipv4_key) + i.getDisplayName(), "10.20.30.40"));
                    addressIpv4EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.address_ipv4_key) + i.getDisplayName(), "10.20.30.40"));

                    overlayManualConfigurationPreferenceCategory.addPreference(addressIpv4EditTextPreference);

                    EditTextPreference portIpv4EditTextPreference = new EditTextPreference(getContext());

                    portIpv4EditTextPreference.setTitle(getString(R.string.port_ipv4) + " " + i.getDisplayName());
                    portIpv4EditTextPreference.setKey(getString(R.string.port_ipv4_key));
                    portIpv4EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {

                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });
                    portIpv4EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.port_ipv4_key) + i.getDisplayName(), "9596"));
                    portIpv4EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.port_ipv4_key) + i.getDisplayName(), "9596"));
                    portIpv4EditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Log.d("aaa", preference.getSummary().toString());
                            //if ((String)newValue && "".equals((String)newValue)) {

                            //}
                            preference.setSummary((String) newValue);
                            return true;
                        }
                    });
                    overlayManualConfigurationPreferenceCategory.addPreference(portIpv4EditTextPreference);

                    EditTextPreference addressIpv6EditTextPreference = new EditTextPreference(getContext());
                    //caceSizeEditTextPreference.setOrder();
                    addressIpv6EditTextPreference.setTitle(getString(R.string.address_ipv6) + " " + i.getDisplayName());
                    addressIpv6EditTextPreference.setKey(getString(R.string.address_ipv6_key) + i.getDisplayName());
                    addressIpv6EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {

                            //editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });
                    addressIpv6EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.address_ipv6_key) + i.getDisplayName(), "ffff::1"));
                    addressIpv6EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.address_ipv6_key) + i.getDisplayName(), "ffff::1"));
                    overlayManualConfigurationPreferenceCategory.addPreference(addressIpv6EditTextPreference);

                    EditTextPreference portIpv6EditTextPreference = new EditTextPreference(getContext());

                    portIpv6EditTextPreference.setTitle(getString(R.string.port_ipv6) + " " + i.getDisplayName());
                    portIpv6EditTextPreference.setKey(getString(R.string.port_ipv6_key));
                    portIpv6EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {

                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });
                    portIpv6EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.port_ipv6_key) + i.getDisplayName(), "9596"));
                    portIpv6EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.port_ipv6_key) + i.getDisplayName(), "9596"));
                    portIpv6EditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Log.d("aaa", preference.getSummary().toString());
                            //if ((String)newValue && "".equals((String)newValue)) {

                            //}
                            preference.setSummary((String) newValue);
                            return true;
                        }
                    });
                    overlayManualConfigurationPreferenceCategory.addPreference(portIpv6EditTextPreference);
                }


            }
        } catch (SocketException e) {
            e.printStackTrace();
        }*/

        addPreferenceByNetworkInterface("WiFi", preferenceScreen);

        addPreferenceByNetworkInterface("Radio", preferenceScreen);

        addPreferenceByNetworkInterface("Wired", preferenceScreen);
    }



    void addPreferenceByNetworkInterface(String interfaceName, PreferenceScreen preferenceScreen) {
        EditTextPreference addressIpv4EditTextPreference = new EditTextPreference(getContext());
        addressIpv4EditTextPreference.setTitle(getString(R.string.nexthop_address_ipv4) + " " + interfaceName);
        addressIpv4EditTextPreference.setKey(getString(R.string.nexthop_address_ipv4_key) + interfaceName);
        addressIpv4EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {

                //editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        addressIpv4EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_address_ipv4_key) + interfaceName, getString(R.string.default_ipv4)));
        addressIpv4EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_address_ipv4_key) + interfaceName, getString(R.string.default_ipv4)));
        addressIpv4EditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                //TODO check if ip is correct
                preference.setSummary((String) newValue);
                return true;
            }
        });
        overlayManualConfigurationPreferenceCategory.addPreference(addressIpv4EditTextPreference);

        EditTextPreference portIpv4EditTextPreference = new EditTextPreference(getContext());

        portIpv4EditTextPreference.setTitle(getString(R.string.nexthop_port_ipv4) + " " + interfaceName);
        portIpv4EditTextPreference.setKey(getString(R.string.nexthop_port_ipv4_key) + interfaceName);
        portIpv4EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        portIpv4EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_port_ipv4_key) + interfaceName, getString(R.string.default_port)));
        portIpv4EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_port_ipv4_key) + interfaceName, getString(R.string.default_port)));
        portIpv4EditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });
        overlayManualConfigurationPreferenceCategory.addPreference(portIpv4EditTextPreference);

        EditTextPreference addressIpv6EditTextPreference = new EditTextPreference(getContext());
        //caceSizeEditTextPreference.setOrder();
        addressIpv6EditTextPreference.setTitle(getString(R.string.nexthop_address_ipv6) + " " + interfaceName);
        addressIpv6EditTextPreference.setKey(getString(R.string.nexthop_address_ipv6_key) + interfaceName);
        addressIpv6EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {

                //editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        addressIpv6EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_address_ipv6_key) + interfaceName, getString(R.string.default_ipv6)));
        addressIpv6EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_address_ipv6_key) + interfaceName, getString(R.string.default_ipv6)));
        addressIpv6EditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });
        overlayManualConfigurationPreferenceCategory.addPreference(addressIpv6EditTextPreference);

        EditTextPreference portIpv6EditTextPreference = new EditTextPreference(getContext());

        portIpv6EditTextPreference.setTitle(getString(R.string.nexthop_port_ipv6) + " " + interfaceName);
        portIpv6EditTextPreference.setKey(getString(R.string.nexthop_port_ipv6_key) + interfaceName);
        portIpv6EditTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
        portIpv6EditTextPreference.setDefaultValue(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_port_ipv6_key) + interfaceName, getString(R.string.default_port)));
        portIpv6EditTextPreference.setSummary(preferenceScreen.getSharedPreferences().getString(getString(R.string.nexthop_port_ipv6_key) + interfaceName, getString(R.string.default_port)));
        portIpv6EditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("aaa", preference.getSummary().toString());
                preference.setSummary((String) newValue);
                return true;
            }
        });
        overlayManualConfigurationPreferenceCategory.addPreference(portIpv6EditTextPreference);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
