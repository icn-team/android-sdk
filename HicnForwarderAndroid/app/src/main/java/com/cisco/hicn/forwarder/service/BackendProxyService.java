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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.forwarder.ForwarderFragment;
import com.cisco.hicn.forwarder.supportlibrary.HProxy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BackendProxyService extends VpnService implements Handler.Callback {
    private static final String TAG = VpnService.class.getSimpleName();

    public static final String ACTION_CONNECT = "com.cisco.hicn.forwarder.START";
    public static final String ACTION_DISCONNECT = "com.cisco.hicn.forwarder.STOP";
    private static final int FAILURE_MESSAGE = 1;

    private Handler mHandler;

    private PendingIntent mConfigureIntent;

    private final AtomicReference<Thread> mConnectingThread = new AtomicReference<>();

    private HProxy mProxyInstance = null;
    private AtomicInteger mNextConnectionId = new AtomicInteger(1);

    @Override
    public void onCreate() {
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        mConfigureIntent = PendingIntent.getActivity(this, 0, new Intent(this, ForwarderFragment.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Starting VPN service");
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            disconnect();
            return START_NOT_STICKY;
        } else {
            connect();
            return START_STICKY;
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        if (message.what != R.string.hproxy_disconnected) {
            updateForegroundNotification(message.what);
        }
        return true;
    }

    private void disconnect() {
        if (mProxyInstance != null) {
            mHandler.sendEmptyMessage(R.string.hproxy_disconnect);
            setConnectingThread(null);
            mProxyInstance.stop();
            mProxyInstance = null;
        }

        stopForeground(true);
        stopSelf();
    }

    private void connect() {
        // Become a foreground service. Background services can be VPN services too, but they can
        // be killed by background check before getting a chance to receive onRevoke().
        updateForegroundNotification(R.string.hproxy_connecting);
        mHandler.sendEmptyMessage(R.string.hproxy_connecting);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String serverAddress = prefs.getString(getString(R.string.hproxy_server_key), getString(R.string.default_hproxy_server));
        final String serverPortString = prefs.getString(getString(R.string.hproxy_server_port_key), getString(R.string.default_hproxy_server_port));
        final int serverPort = Integer.parseInt(serverPortString);
        final String secret = prefs.getString(getString(R.string.hproxy_secret_key), getString(R.string.default_hproxy_secret));

        this.startConnection(new ProxyThread(this, mNextConnectionId.getAndIncrement(),
                serverAddress, serverPort, secret));
    }

    private void startConnection(final ProxyThread proxy) {
        // Get an instance of the proxy
        mProxyInstance = HProxy.getInstance();
        // Replace any existing connecting thread with the  new one.
        final Thread thread = new Thread(proxy, "HProxyBackendThread");
        setConnectingThread(thread);

        proxy.setConfigureIntent(mConfigureIntent);
        proxy.setOnEstablishListener(tunInterface -> {
            mHandler.sendEmptyMessage(R.string.hproxy_connected);
            mConnectingThread.compareAndSet(null, thread);
        });

        thread.start();
    }

    private void setConnectingThread(final Thread thread) {
        final Thread oldThread = mConnectingThread.getAndSet(thread);
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }

    private void updateForegroundNotification(final int message) {
        final String NOTIFICATION_CHANNEL_ID = "HProxy";
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT));
        startForeground(1, new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentText(getString(message))
                .setContentIntent(mConfigureIntent)
                .build());
    }

    private void updateForegroundNotification(final String message) {
        final String NOTIFICATION_CHANNEL_ID = "HProxy";
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT));
        startForeground(1, new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentText(message)
                .setContentIntent(mConfigureIntent)
                .build());
    }



}