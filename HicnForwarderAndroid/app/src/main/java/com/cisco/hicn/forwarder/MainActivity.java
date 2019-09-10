package com.cisco.hicn.forwarder;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.preference.PreferenceManager;
//import android.support.annotation.NonNull;
//import android.support.design.widget.NavigationView;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
import com.google.android.material.navigation.NavigationView;
import android.os.Bundle;
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

import com.cisco.hicn.forwarder.R;

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
        mDrawer = (DrawerLayout)findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this,mDrawer,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nav_View = (NavigationView)findViewById(R.id.nav_View);
        nav_View.setNavigationItemSelectedListener(this);
        viewLayout = (FrameLayout)findViewById(R.id.viewLayout);
        fragmentManager = getSupportFragmentManager();

        // Declare fragments here
        home = new Home();
        settings = new PreferencesFragment();
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
      //  String aaa = sharedPreferences.getString("username", "ciao!!!");
        //Log.d("aaa", aaa);
        //Log.d("aaa", settings.getArguments().getString("username"));

        /*msg = new Message();
        video = new Video();
        noti = new Notification();
        contact = new ContactUs();
*/

        //fragmentManager.beginTransaction().replace(R.id.viewLayout, home).commit();
        fragmentManager.beginTransaction().replace(R.id.viewLayout, settings).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)){
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
            /*case R.id.action_msg:
                fragment = home;
                setTitle(menuItem.getTitle());
                break;
            case R.id.action_video:
                fragment = home;
                setTitle(menuItem.getTitle());
                break;
            case R.id.action_info:
                fragment = home;
                setTitle(menuItem.getTitle());
                break;*/
            //case R.id.action_home:
            //    fragment = home;
            //    setTitle(menuItem.getTitle());
            //    break;
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
    public void onFragmentInteraction(Uri uri){
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