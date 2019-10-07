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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cisco.hicn.forwarder.ForwarderAndroidActivity;
import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.supportlibrary.NetworkServiceHelper;
import com.cisco.hicn.forwarder.supportlibrary.SocketBinder;
import com.cisco.hicn.forwarder.utility.Constants;
import com.cisco.hicn.forwarder.utility.ResourcesEnumerator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Native;

public class BackendAndroidService extends Service {
    private final static String TAG = "BackendService";

    private static Thread sForwarderThread = null;
    private static Thread sFacemgrThread = null;

    private NetworkServiceHelper mNetService = new NetworkServiceHelper();
    private SocketBinder mSocketBinder = new SocketBinder();

    public BackendAndroidService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int capacity;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NativeAccess nativeAccess = NativeAccess.getInstance();
        if (!nativeAccess.isRunningForwarder()) {
            Log.d(TAG, "Starting Backand Service");


            mNetService.init(this, mSocketBinder);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_START_FORWARDER), 1000); // wait for mobile network is up


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
        mNetService.clear();
        super.onDestroy();
    }

    private static final int EVENT_START_FORWARDER = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_START_FORWARDER:

                    getCapacity();
                    startBackend();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void getCapacity() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.FORWARDER_PREFERENCES, MODE_PRIVATE);
        this.capacity = Integer.parseInt(sharedPreferences.getString(ResourcesEnumerator.CAPACITY.key(), Constants.DEFAULT_CAPACITY));
        ;
    }

    protected Runnable mForwarderRunner = new Runnable() {

        //private String path;
        @Override
        public void run() {
            NativeAccess nativeAccess = NativeAccess.getInstance();
            nativeAccess.setSocketBinder(mSocketBinder);
            nativeAccess.startForwarder(capacity);
        }
    };

    protected Runnable mFacemgrRunner = new Runnable() {

        //private String path;
        @Override
        public void run() {
            NativeAccess nativeAccess = NativeAccess.getInstance();

            nativeAccess.startFacemgr();
        }


    };

    private void startBackend() {
        String NOTIFICATION_CHANNEL_ID = "12345";
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);

            Intent notificationIntent = new Intent(this, ForwarderAndroidActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentTitle("ForwarderAndroid").setContentText("ForwarderAndroid").setOngoing(true).setContentIntent(activity);
            notification = notificationBuilder.build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle("ForwarderAndroid")
                    .setContentText("ForwarderAndroid")
                    .build();
        }

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "ForwarderAndroid", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("ForwarderAndroid");
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