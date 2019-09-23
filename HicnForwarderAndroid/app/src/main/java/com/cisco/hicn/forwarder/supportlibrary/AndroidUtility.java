package com.cisco.hicn.forwarder.supportlibrary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;

import com.cisco.hicn.forwarder.MainActivity;

public class AndroidUtility {

    public static int getNetworkType(String networkName) {
        //if (MainActivity.interfacesHashMap.containsKey(networkName)) {
        //    return MainActivity.interfacesHashMap.get(networkName);
        //} else {
        return getNetworkType(MainActivity.context, networkName);
        //if (type > -1)
        //    MainActivity.interfacesHashMap.put(networkName, type);
        //return type;
        //  }
    }

    public static int getNetworkType(Context context, String networkName) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return -1; //error
        }
        for (Network network : connectivityManager.getAllNetworks()) {
            LinkProperties prop = connectivityManager.getLinkProperties(network);
            if (prop.getInterfaceName()!= null &&  prop.getInterfaceName().equals(networkName.trim())) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities == null) {
                    return -1; //error
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return 1;
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return 2;
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return 3;
                }
                return 0; //not supported
            }
        }
        return -1; //error
    }

}
