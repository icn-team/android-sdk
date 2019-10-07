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

package com.cisco.hicn.forwarder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cisco.hicn.forwarder.forwarder.ForwarderFragment;
import com.cisco.hicn.forwarder.preferences.PreferencesFragment;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.utility.NetdeviceTypeEnum;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements Home.OnFragmentInteractionListener,
        PreferencesFragment.OnFragmentInteractionListener,
        ForwarderFragment.OnFragmentInteractionListener,
        InterfaceFragment.OnFragmentInteractionListener,
        ApplicationsFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private NavigationView nav_View;
    private FragmentManager fragmentManager;
    private FrameLayout viewLayout;


    private Home home;
    private PreferencesFragment settings;

    public static Context context;
    //public static HashMap<String, Integer> interfacesHashMap = new HashMap<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkEnabledPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkEnabledPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        checkEnabledPermission(Manifest.permission.FOREGROUND_SERVICE);

        MainActivity.context = getApplicationContext();

       // fillInterfaceTypes();

        setContentView(R.layout.activity_main);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nav_View = (NavigationView) findViewById(R.id.nav_View);
        nav_View.setNavigationItemSelectedListener(this);
        viewLayout = (FrameLayout) findViewById(R.id.viewLayout);
        fragmentManager = getSupportFragmentManager();

        // Declare fragments here
        home = new Home();
        settings = new PreferencesFragment();

        NativeAccess nativeAccess = NativeAccess.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean enableBonjour = sharedPreferences.getBoolean(getString(R.string.enable_bonjour_key), false);
        nativeAccess.enableDiscovery(enableBonjour);

        boolean enableNexHopIPv4 = sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv4_key), true);
        nativeAccess.disableIPv4(!enableNexHopIPv4);

        boolean enableNexHopIPv6 = sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv6_key), true);
        nativeAccess.disableIPv6(!enableNexHopIPv6);

        boolean enableWifi = sharedPreferences.getBoolean(getString(R.string.enable_wifi_key), true);

        if (enableWifi) {

            int wifiSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_source_port_ipv4_key), getString(R.string.default_wifi_source_port_ipv4)));
            String wifiNextHopIPv4 = sharedPreferences.getString(getString(R.string.wifi_nexthop_ipv4_key), getString(R.string.default_wifi_nexthop_ipv4));
            int wifiNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_nexthop_port_ipv4_key), getString(R.string.default_wifi_nexthop_port_ipv4)));
            nativeAccess.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIFI.getValue(), wifiSourcePortIPv4, wifiNextHopIPv4, wifiNextHopPortIPv4);

            int wifiSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_source_port_ipv6_key), getString(R.string.default_wifi_source_port_ipv6)));
            String wifiNextHopIPv6 = sharedPreferences.getString(getString(R.string.wifi_nexthop_ipv6_key), getString(R.string.default_wifi_nexthop_ipv6));
            int wifiNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_nexthop_port_ipv6_key), getString(R.string.default_wifi_nexthop_port_ipv6)));
            nativeAccess.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIFI.getValue(), wifiSourcePortIPv6, wifiNextHopIPv6, wifiNextHopPortIPv6);
        }

        boolean cellularWifi = sharedPreferences.getBoolean(getString(R.string.enable_cellular_key), true);

        if (cellularWifi) {
            int cellularSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv4_key), getString(R.string.default_cellular_source_port_ipv4)));
            String cellularNextHopIPv4 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv4_key), getString(R.string.default_cellular_nexthop_ipv4));
            int cellularNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv4_key), getString(R.string.default_cellular_nexthop_port_ipv4)));
            nativeAccess.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_CELLULAR.getValue(), cellularSourcePortIPv4, cellularNextHopIPv4, cellularNextHopPortIPv4);

            int cellularSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv6_key), getString(R.string.default_cellular_source_port_ipv6)));
            String cellularNextHopIPv6 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv6_key), getString(R.string.default_cellular_nexthop_ipv6));
            int cellularNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv6_key), getString(R.string.default_cellular_nexthop_port_ipv6)));
            nativeAccess.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_CELLULAR.getValue(), cellularSourcePortIPv6, cellularNextHopIPv6, cellularNextHopPortIPv6);
        }

        boolean wiredWifi = sharedPreferences.getBoolean(getString(R.string.enable_wired_key), true);

        if (wiredWifi) {
            int wiredSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv4_key), getString(R.string.default_wired_source_port_ipv4)));
            String wiredNextHopIPv4 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv4_key), getString(R.string.default_wired_nexthop_ipv4));
            int wiredNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv4_key), getString(R.string.default_wired_nexthop_port_ipv4)));
            nativeAccess.updateInterfaceIPv4(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv4, wiredNextHopIPv4, wiredNextHopPortIPv4);

            int wiredSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv6_key), getString(R.string.default_wired_source_port_ipv6)));
            String wiredNextHopIPv6 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv6_key), getString(R.string.default_wired_nexthop_ipv6));
            int wiredNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv6_key), getString(R.string.default_wired_nexthop_port_ipv6)));
            nativeAccess.updateInterfaceIPv6(NetdeviceTypeEnum.NETDEVICE_TYPE_WIRED.getValue(), wiredSourcePortIPv6, wiredNextHopIPv6, wiredNextHopPortIPv6);
        }
        fragmentManager.beginTransaction().replace(R.id.viewLayout, home).commit();

    }

    private void checkEnabledPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.settings:
                fragment = settings;
                setTitle(menuItem.getTitle());
                break;
            default:
                fragment = home;
                setTitle(menuItem.getTitle());
                break;

        }
        fragmentManager.beginTransaction().replace(R.id.viewLayout, fragment).commit();
        mDrawer.closeDrawers();
        return true;

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }


}