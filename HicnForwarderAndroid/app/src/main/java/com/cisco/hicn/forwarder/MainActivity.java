package com.cisco.hicn.forwarder;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cisco.hicn.forwarder.preferences.PreferencesFragment;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.utility.Constants;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;

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
    public static HashMap<String, Integer> interfacesHashMap = new HashMap<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.context = getApplicationContext();

        fillInterfaceTypes();


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

        boolean enableBonjour = sharedPreferences.getBoolean(getString(R.string.enable_bonjour_key), true);
        nativeAccess.disableDiscovery(!enableBonjour);

        boolean enableNexHopIPv4 = sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv4_key), true);
        nativeAccess.disableIPv4(!enableNexHopIPv4);

        boolean enableNexHopIPv6 = sharedPreferences.getBoolean(getString(R.string.enable_nexthop_ipv6_key), true);
        nativeAccess.disableIPv6(!enableNexHopIPv6);

        boolean enableWifi = sharedPreferences.getBoolean(getString(R.string.enable_wifi_key), true);

        if (enableWifi) {

            int wifiSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_source_port_ipv4_key), getString(R.string.default_wifi_source_port_ipv4)));
            String wifiNextHopIPv4 = sharedPreferences.getString(getString(R.string.wifi_nexthop_ipv4_key), getString(R.string.default_wifi_nexthop_ipv4));
            int wifiNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_nexthop_port_ipv4_key), getString(R.string.default_wifi_nexthop_port_ipv4)));
            nativeAccess.updateInterfaceIPv4(Constants.NETDEVICE_TYPE_WIFI, wifiSourcePortIPv4, wifiNextHopIPv4, wifiNextHopPortIPv4);

            int wifiSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_source_port_ipv6_key), getString(R.string.default_wifi_source_port_ipv6)));
            String wifiNextHopIPv6 = sharedPreferences.getString(getString(R.string.wifi_nexthop_ipv6_key), getString(R.string.default_wifi_nexthop_ipv6));
            int wifiNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wifi_nexthop_port_ipv6_key), getString(R.string.default_wifi_nexthop_port_ipv6)));
            nativeAccess.updateInterfaceIPv6(Constants.NETDEVICE_TYPE_WIFI, wifiSourcePortIPv6, wifiNextHopIPv6, wifiNextHopPortIPv6);
        }

        boolean cellularWifi = sharedPreferences.getBoolean(getString(R.string.enable_cellular_key), true);

        if (cellularWifi) {
            int cellularSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv4_key), getString(R.string.default_cellular_source_port_ipv4)));
            String cellularNextHopIPv4 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv4_key), getString(R.string.default_cellular_nexthop_ipv4));
            int cellularNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv4_key), getString(R.string.default_cellular_nexthop_port_ipv4)));
            nativeAccess.updateInterfaceIPv4(Constants.NETDEVICE_TYPE_CELLULAR, cellularSourcePortIPv4, cellularNextHopIPv4, cellularNextHopPortIPv4);

            int cellularSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_source_port_ipv6_key), getString(R.string.default_cellular_source_port_ipv6)));
            String cellularNextHopIPv6 = sharedPreferences.getString(getString(R.string.cellular_nexthop_ipv6_key), getString(R.string.default_cellular_nexthop_ipv6));
            int cellularNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.cellular_nexthop_port_ipv6_key), getString(R.string.default_cellular_nexthop_port_ipv6)));
            nativeAccess.updateInterfaceIPv6(Constants.NETDEVICE_TYPE_CELLULAR, cellularSourcePortIPv6, cellularNextHopIPv6, cellularNextHopPortIPv6);
        }

        boolean wiredWifi = sharedPreferences.getBoolean(getString(R.string.enable_cellular_key), true);

        if (wiredWifi) {
            int wiredSourcePortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv4_key), getString(R.string.default_wired_source_port_ipv4)));
            String wiredNextHopIPv4 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv4_key), getString(R.string.default_wired_nexthop_ipv4));
            int wiredNextHopPortIPv4 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv4_key), getString(R.string.default_wired_nexthop_port_ipv4)));
            nativeAccess.updateInterfaceIPv4(Constants.NETDEVICE_TYPE_WIRED, wiredSourcePortIPv4, wiredNextHopIPv4, wiredNextHopPortIPv4);

            int wiredSourcePortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_source_port_ipv6_key), getString(R.string.default_wired_source_port_ipv6)));
            String wiredNextHopIPv6 = sharedPreferences.getString(getString(R.string.wired_nexthop_ipv6_key), getString(R.string.default_wired_nexthop_ipv6));
            int wiredNextHopPortIPv6 = Integer.parseInt(sharedPreferences.getString(getString(R.string.wired_nexthop_port_ipv6_key), getString(R.string.default_wired_nexthop_port_ipv6)));
            nativeAccess.updateInterfaceIPv6(Constants.NETDEVICE_TYPE_WIRED, wiredSourcePortIPv6, wiredNextHopIPv6, wiredNextHopPortIPv6);
        }
        fragmentManager.beginTransaction().replace(R.id.viewLayout, home).commit();

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


    public void fillInterfaceTypes() {
        interfacesHashMap.clear();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        for (Network network : connectivityManager.getAllNetworks()) {
            LinkProperties prop = connectivityManager.getLinkProperties(network);
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return; //error
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                interfacesHashMap.put(prop.getInterfaceName(), 1);
                continue;
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                interfacesHashMap.put(prop.getInterfaceName(), 2);
                continue;
            }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                interfacesHashMap.put(prop.getInterfaceName(), 3);
                continue;
            }
        }
    }
}