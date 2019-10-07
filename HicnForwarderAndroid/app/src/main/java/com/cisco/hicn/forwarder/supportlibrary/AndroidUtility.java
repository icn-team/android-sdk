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

package com.cisco.hicn.forwarder.supportlibrary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.cisco.hicn.forwarder.MainActivity;
import com.cisco.hicn.forwarder.utility.Constants;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Queue;

public class AndroidUtility {

    private static AndroidUtility sInstance = null;

    private Queue<Integer> hiperfGraphQueue;

    public static AndroidUtility getInstance() {
        if (sInstance == null) {
            sInstance = new AndroidUtility();
        }
        return sInstance;
    }

    public static int getNetworkType(String networkName) {
        return getNetworkType(MainActivity.context, networkName);
    }

    public static int getNetworkType(Context context, String networkName) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return -1; //error
        }

        for (Network network : connectivityManager.getAllNetworks()) {
            LinkProperties prop = connectivityManager.getLinkProperties(network);
            if (prop.getInterfaceName() != null && prop.getInterfaceName().equals(networkName.trim())) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities == null) {
                    return Constants.AU_INTERFACE_TYPE_UNDEFINED; //error
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return Constants.AU_INTERFACE_TYPE_WIRED;
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return Constants.AU_INTERFACE_TYPE_WIFI;
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return Constants.AU_INTERFACE_TYPE_CELLULAR;
                }
                return Constants.AU_INTERFACE_TYPE_UNDEFINED; //not supported
            }

        }

        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(networkName);
            if (networkInterface.isLoopback())
                return Constants.AU_INTERFACE_TYPE_LOOPBACK;
        } catch (SocketException e) {
            Log.d(AndroidUtility.class.getCanonicalName(), "error");
        }
        return Constants.AU_INTERFACE_TYPE_UNDEFINED; //error
    }

    public void setHiperfGraphQueue(Queue<Integer> hiperfGraphQueue) {
        this.hiperfGraphQueue = hiperfGraphQueue;
    }

   public Queue<Integer>getHiperfGraphQueue() {
        return hiperfGraphQueue;
   }

    static void pushGoodput(int goodput) {
        Log.d("hiperf", "goodput: " + goodput);
        AndroidUtility.getInstance().getHiperfGraphQueue().add(goodput);
    }

}
