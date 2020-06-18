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

import com.cisco.hicn.forwarder.service.ProxyBackend;
import java.util.Properties;

public class HProxy {
//    private static HProxy sInstance = null;
    private ProxyBackend mProxyBackend;

    // This will sotre the actual pointer to the proxy
    private long mProxyPtr = 0;

    static {
        System.loadLibrary("hproxy-wrapper");
    }

    public HProxy() { initConfig(); }

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
}
