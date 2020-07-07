/*
 * Copyright (c) 2019-2020 Cisco and/or its affiliates.
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

import android.app.Service;
import android.util.Log;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.HProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class ProxyBackendNative extends ProxyBackend {
    /**
     * Name of the hicn tun interface
     */
    private static final String TUN_INTERFACE = "TestTun2";

    public ProxyBackendNative(Service parentService) {
        super(parentService);
    }

    @Override
    public int configureTun(Properties parameters) {
        if (mTunFd != -1) {
            Log.w(getTag(), "File descriptor already assigned.");
            return mTunFd;
        }

        String address = null;
        try {
            address = parameters.getProperty("ADDRESS") + "/" + parameters.getProperty("PREFIX_LENGTH");
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad parameter");
        }

        mParameters = parameters;

        synchronized (mParentService) {
            mTunFd = startHicnTun(TUN_INTERFACE, mProxiedPackages, address);
            mHandler.sendEmptyMessage(R.string.hproxy_connected);
        }

        Log.i(getTag(), "New interface: " + TUN_INTERFACE + " (" + address + ")");

        return mTunFd;
    }

    @Override
    public int closeTun() {
        if (mTunFd != -1) {
            stopHicn();
        }

        return 0;
    }

    private int startHicnTun(String interfaceName, String[] packageName, String ipAddress) {
        int fd = -1;
        if (getHicnService()) {
            try {
                Log.d(getTag(), "Opening tun device  " + interfaceName + " with IP address " + ipAddress);
                fd = HProxy.getInstance().getTunFd(interfaceName);
                Method startHicn = sIHicnManagerClass.getMethod("startHicn", String.class, String[].class, String.class);
                startHicn.invoke(sHicnManager, interfaceName, packageName, ipAddress);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        Log.d(getTag(), "The PID of the app is " + android.os.Process.myPid());
        return fd;
    }

    private void stopHicn() {
        if (getHicnService()) {
            try {
                Method stop = sIHicnManagerClass.getMethod("stopHicn");
                stop.invoke(sHicnManager);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private final String getTag() {
        return ProxyBackendNative.class.getSimpleName();
    }
}
