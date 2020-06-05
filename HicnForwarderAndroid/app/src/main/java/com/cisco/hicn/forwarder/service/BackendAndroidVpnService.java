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

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.util.Log;

public class BackendAndroidVpnService extends VpnService {
    private static final String TAG = VpnService.class.getSimpleName();

    public static final String ACTION_CONNECT = "com.cisco.hicn.forwarder.START";
    public static final String ACTION_DISCONNECT = "com.cisco.hicn.forwarder.STOP";

    private PendingIntent mConfigureIntent;
    private ProxyBackend mProxyBackend = null;

    @Override
    public void onCreate() {
//        mConfigureIntent = PendingIntent.getActivity(this, 0, new Intent(this, ForwarderFragment.class),
//                PendingIntent.FLAG_UPDATE_CURRENT);

        if (mProxyBackend == null) {
            mProxyBackend = ProxyBackend.getProxyBackend(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            Log.v(TAG, "Stopping VPN service");
            mProxyBackend.disconnect();
        } else if (mProxyBackend.connect()) {
            Log.v(TAG, "Starting VPN service");
            return START_STICKY;
        }

        stopForeground(true);
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onRevoke() {
        Log.v(TAG, "Vpn Revoked.");
        mProxyBackend.disconnect();
        stopForeground(true);
        stopSelf();
        super.onRevoke();
    }
}