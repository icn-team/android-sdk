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

package com.cisco.hicn.forwarder.supportlibrary;

import com.cisco.hicn.forwarder.service.BackendAndroidService;
import com.cisco.hicn.forwarder.service.ProxyBackend;
import android.app.Service;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class HProxy {
    private static HProxy sInstance = null;
    private static Activity mActivity = null;

    private ProxyBackend mProxyBackend;

    // This will sotre the actual pointer to the proxy (in JNI code)
    private long mProxyPtr = 0;

    /** Messenger for communicating with the service. */
    Messenger mMessenger = null;

    /** We need a context to bind to a Service */
    Service mService;

    /** Flag indicating whether we have called bind on the service. */
    boolean bound;

    static {
        System.loadLibrary("hproxy-wrapper");
    }

    public static HProxy getInstance() {
        if (sInstance == null) {
            sInstance = new HProxy();
        }
        return sInstance;
    }

    public static void stopInstance() {
        if (sInstance == null)
            return;
        sInstance.stop();
        sInstance = null;
    }

    public HProxy() {
        initConfig();
    }

    public void setService(Service service) {
        mService = service;
    }

    public static void setActivity(Activity activity) {
        mActivity = activity;
    }

    public static Activity getActivity() {
        return mActivity;
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mMessenger = new Messenger(service);
            bound = true;

            Forwarder forwarder = Forwarder.getInstance();
            while (!forwarder.isRunningForwarder()) {
                // wait for the forwarder ro run.
                Log.d(getTag(), "Hicn forwarder is not started yet. Waiting before activating the proxy.");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) { }
            }

            Log.i(getTag(), "hICN service is now available");
            onHicnServiceAvailable(true);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // NOTE: You will never receive a call to onServiceDisconnected()
            // for a Service running in the same process that you’re binding
            // from.

            mMessenger = null;
            bound = false;

            Log.i(getTag(), "hICN service is no more available");
            onHicnServiceAvailable(false);
        }
    };

    public void setProxyInstance(ProxyBackend proxyThread) {
        mProxyBackend = proxyThread;
    }

    public native void initConfig();

    public int createTunDevice(String vpn_address, int prefix_length,
                               String route_address,
                               int route_prefix_length, String dns) {
        Properties params = new Properties();
        params.put("ADDRESS", vpn_address);
        params.put("PREFIX_LENGTH", Integer.toString(prefix_length));
        params.put("ROUTE_ADDRESS",route_address);
        params.put("ROUTE_PREFIX_LENGTH", Integer.toString(route_prefix_length));
        params.put("DNS", dns);
        int ret = mProxyBackend.configureTun(params);
        return ret;
    }

    public int closeTunDevice() {
        return mProxyBackend.closeTun();
    }

    public native boolean isRunning();

    public native int getTunFd(String device_name);

    public native void start(String remote_address, int remote_port);

    public native void stop();

    public native void destroy();

    public static native boolean isHProxyEnabled();

    public static native String getProxifiedAppName();
    public static native String getProxifiedPackageName();
    public static native String getHicnServiceName();

    // BEGIN WITH_START_STOP

    public int attachHicnService() {
        Log.i(getTag(), "Attaching hICN service");

        mService.bindService(new Intent(mService, BackendAndroidService.class), mConnection,
            Context.BIND_AUTO_CREATE);
        return 0;
    }

    public int detachHicnService() {
        Log.i(getTag(), "Detaching hICN service");

        if (bound) {
            mService.unbindService(mConnection);

            /*
             * We need to process event here as we are in the same process and
             * onServiceDisconnected will not be called
             */
            mMessenger = null;
            bound = false;

            Log.i(getTag(), "hICN service is no more available");
            onHicnServiceAvailable(false);
        }
        return 0;
    }

    public native void onHicnServiceAvailable(boolean flag);

    // END WITH_START_STOP

    private final String getTag() {
        return HProxy.class.getSimpleName();
    }
}
