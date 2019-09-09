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

package com.cisco.hicn.forwarder.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cisco.hicn.forwarder.ForwarderAndroidActivity;
import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.utility.Constants;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class BackendAndroidService extends Service {
    private final static String TAG = "BackendService";

    private static Thread sForwarderThread = null;
    private static Thread sFacemgrThread = null;

    public BackendAndroidService() {
    }

    private int capacity = 0;

    private String overlayDiscovery = null;

    private String nextHopIpV4Wifi = null;
    private int nextHopIpV4PortWifi = 9596;

    private String nextHopIpV6Wifi = null;
    private int nextHopIpV6PortWifi = 9596;

    private String nextHopIpV4Radio = null;
    private int nextHopIpV4PortRadio = 9596;

    private String nextHopIpV6Radio = null;
    private int nextHopIpV6PortRadio = 9596;

    private String nextHopIpV4Wired = null;
    private int nextHopIpV4PortWired = 9596;

    private String nextHopIpV6Wired = null;
    private int nextHopIpV6PortWired = 9596;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NativeAccess nativeAccess = NativeAccess.getInstance();
        if (!nativeAccess.isRunningForwarder()) {
            Log.d(TAG, "Starting Backand Service");
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
            capacity = Integer.parseInt(sharedPreferences.getString(getString(R.string.cache_size_key), "0"));

            overlayDiscovery = sharedPreferences.getString(getString(R.string.overlay_discovery_key), getString(R.string.default_overlay_discovery));

            if (getString(R.string.manual).equals(overlayDiscovery)) {

                nextHopIpV4Wifi = sharedPreferences.getString(getString(R.string.nexthop_address_ipv4_key) + getString(R.string.wifi), getString(R.string.default_ipv4));

                nextHopIpV6Wifi = sharedPreferences.getString(getString(R.string.nexthop_address_ipv6_key) + getString(R.string.wifi), getString(R.string.default_ipv6));

                nextHopIpV4Radio = sharedPreferences.getString(getString(R.string.nexthop_address_ipv4_key) + getString(R.string.radio), getString(R.string.default_ipv4));

                nextHopIpV6Radio = sharedPreferences.getString(getString(R.string.nexthop_address_ipv6_key) + getString(R.string.radio), getString(R.string.default_ipv6));

                nextHopIpV4Wired = sharedPreferences.getString(getString(R.string.nexthop_address_ipv4_key) + getString(R.string.wired), getString(R.string.default_ipv4));

                nextHopIpV6Wired = sharedPreferences.getString(getString(R.string.nexthop_address_ipv6_key) + getString(R.string.wired), getString(R.string.default_ipv6));

                nextHopIpV4PortWifi = Integer.parseInt(sharedPreferences.getString(getString(R.string.nexthop_port_ipv4_key) + getString(R.string.wifi), getString(R.string.default_port)));

                nextHopIpV6PortWifi = Integer.parseInt(sharedPreferences.getString(getString(R.string.nexthop_port_ipv6_key) + getString(R.string.wifi), getString(R.string.default_port)));

                nextHopIpV4PortRadio = Integer.parseInt(sharedPreferences.getString(getString(R.string.nexthop_port_ipv4_key) + getString(R.string.radio), getString(R.string.default_port)));

                nextHopIpV6PortRadio = Integer.parseInt(sharedPreferences.getString(getString(R.string.nexthop_port_ipv6_key) + getString(R.string.radio), getString(R.string.default_port)));

                nextHopIpV4PortWired = Integer.parseInt(sharedPreferences.getString(getString(R.string.nexthop_port_ipv4_key) + getString(R.string.wired), getString(R.string.default_port)));

                nextHopIpV6PortWired = Integer.parseInt(sharedPreferences.getString(getString(R.string.nexthop_port_ipv6_key) + getString(R.string.wired), getString(R.string.default_port)));


                Log.d(TAG, "overlayDiscovery: " + overlayDiscovery);

                Log.d(TAG, "nextHopIpV4Wifi: " + nextHopIpV4Wifi + ", nextHopIpV4PortWifi:" + nextHopIpV4PortWifi);

                Log.d(TAG, "nextHopIpV6Wifi: " + nextHopIpV6Wifi + ", nextHopIpV6PortWifi:" + nextHopIpV6PortWifi);

                Log.d(TAG, "nextHopIpV4WRadio: " + nextHopIpV4Radio + ", nextHopIpV4PortRadio:" + nextHopIpV4PortRadio);

                Log.d(TAG, "nextHopIpV6Radio: " + nextHopIpV6Radio + ", nextHopIpV6PortRadio" + nextHopIpV6PortRadio);

                Log.d(TAG, "nextHopIpV4Wired: " + nextHopIpV4Wired + ", nextHopIpV4PortWired:" + nextHopIpV4PortWired);

                Log.d(TAG, "nextHopIpV6Wired: " + nextHopIpV6Wired + ", nextHopIpV6PortWired:" + nextHopIpV6PortWired);


            }

            startBackend(intent);


        } else {
            Log.d(TAG, "Forwarder already running.");
        }
        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        NativeAccess nativeAccess = NativeAccess.getInstance();
        Log.d(TAG, "Destroying Forwarder");
        if (nativeAccess.isRunningFacemgr()) {
            nativeAccess.stopFacemgr();
        }

        if (nativeAccess.isRunningForwarder()) {
            nativeAccess.stopForwarder();

            stopForeground(true);
        }
        super.onDestroy();
    }

    protected Runnable mForwarderRunner = new Runnable() {

        //private String path;
        @Override
        public void run() {
            NativeAccess nativeAccess = NativeAccess.getInstance();

            Log.d(TAG, "capacity: " + capacity);
            nativeAccess.startForwarder(capacity);
        }


    };

    protected Runnable mFacemgrRunner = new Runnable() {

        //private String path;
        @Override
        public void run() {
            NativeAccess nativeAccess = NativeAccess.getInstance();
            if (getString(R.string.manual).equals(overlayDiscovery)) {
                Log.d(TAG, "nextHopIpV4Wifi: " + nextHopIpV4Wifi + ", nextHopIpV4PortWifi:" + nextHopIpV4PortWifi);
                nativeAccess.startFacemgrWithConfig(nextHopIpV4Wifi, nextHopIpV4PortWifi,
                        nextHopIpV6Wifi, nextHopIpV6PortWifi,
                        nextHopIpV4Radio, nextHopIpV4PortRadio,
                        nextHopIpV6Radio, nextHopIpV6PortRadio,
                        nextHopIpV4Wired, nextHopIpV4PortWired,
                        nextHopIpV6Wired, nextHopIpV4PortWired);

            } else {
                nativeAccess.startFacemgr();
            }
        }


    };

 /*   private boolean writeToFile(String data, String path) {
        Log.v(TAG, path + " " + data);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), "utf-8"))) {
            writer.write(data);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
            return false;
        }
    }
*/

    private void startBackend(Intent intent) {
        String NOTIFICATION_CHANNEL_ID = "12345";
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);

            Intent notificationIntent = new Intent(this, ForwarderAndroidActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentTitle("BackendAndroid").setContentText("BackendAndroid").setOngoing(true).setContentIntent(activity);
            notification = notificationBuilder.build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle("BackendAndroid")
                    .setContentText("BackendAndroid")
                    .build();
        }

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "BackendAndroid", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("BackendAndroid");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

        }

        startForeground(Constants.FOREGROUND_SERVICE, notification);


        NativeAccess nativeAccess = NativeAccess.getInstance();
        if (!nativeAccess.isRunningForwarder()) {
            sForwarderThread = new Thread(mForwarderRunner, "BackendAndroid");
            sForwarderThread.start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!nativeAccess.isRunningFacemgr()) {
            sFacemgrThread = new Thread(mFacemgrRunner, "BackendAndroid");
            sFacemgrThread.start();
        }


        Log.i(TAG, "BackendAndroid starterd");

    }

}