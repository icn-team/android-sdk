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
import android.net.VpnService;
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

    private Handler mHandler;

    private static class Proxy extends Pair<Thread, ParcelFileDescriptor> {
        public Proxy(Thread thread, ParcelFileDescriptor pfd) {
            super(thread, pfd);
        }
    }

    private PendingIntent mConfigureIntent;

    private final AtomicReference<Thread> mConnectingThread = new AtomicReference<>();
    private final AtomicReference<Proxy> mProxy = new AtomicReference<>();

    private HProxy mProxyInstance = null;
    private AtomicInteger mNextConnectionId = new AtomicInteger(1);

    @Override
    public void onCreate() {
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }
        // Create the intent to "configure" the connection (just start ToyVpnClient).
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
    public void onDestroy() {
        disconnect();
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
        mHandler.sendEmptyMessage(R.string.hproxy_disconnect);
        setConnectingThread(null);
        setProxy(null);

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
        final String hicnConsumerName = prefs.getString(getString(R.string.hproxy_consumer_name_key), getString(R.string.default_hproxy_consumer_name));
        final String hicnProducerName = prefs.getString(getString(R.string.hproxy_producer_name_key), getString(R.string.default_hproxy_producer_name));

        this.startConnection(new ProxyThread(this, mNextConnectionId.getAndIncrement(),
                serverAddress, serverPort, secret, hicnConsumerName, hicnProducerName, this));
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
            mConnectingThread.compareAndSet(thread, null);
            setProxy(new Proxy(thread, tunInterface));
        });
        thread.start();
    }

    private void setConnectingThread(final Thread thread) {
        final Thread oldThread = mConnectingThread.getAndSet(thread);
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }

    private void setProxy(final Proxy proxy) {
        final Proxy oldProxy = mProxy.getAndSet(proxy);
        if (oldProxy != null) {
            try {
                oldProxy.first.interrupt();
                oldProxy.second.close();
            } catch (IOException e) {
                Log.e(TAG, "Closing VPN interface", e);
            }
        }
    }

    private void updateForegroundNotification(final int message) {
        /*final String NOTIFICATION_CHANNEL_ID = "HProxy";

        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentTitle("HProxy").setContentText("HProxy").setOngoing(true).setContentIntent(activity);
            notification = notificationBuilder.setAutoCancel(true).build();

        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle("HProxy")
                    .setContentText("HProxy").setAutoCancel(true)
                    .build();
        }

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "HProxy", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("HProxy");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        startForeground(Constants.FOREGROUND_SERVICE, notification);*/
        final String NOTIFICATION_CHANNEL_ID = "ToyVpn";
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



}