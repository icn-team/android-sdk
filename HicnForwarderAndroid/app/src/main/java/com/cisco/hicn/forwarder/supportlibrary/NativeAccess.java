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

public class NativeAccess {

    private static NativeAccess sInstance = null;

    static {
        System.loadLibrary("cwrap-lib");
        System.loadLibrary("cppwrap-lib");
    }

    public static NativeAccess getInstance() {
        if (sInstance == null) {
            sInstance = new NativeAccess();
        }
        return sInstance;
    }

    private NativeAccess() {
        initConfig();
    }

    public native void initConfig();

    public native boolean isRunningForwarder();

    public native void startForwarder(int capacity);

    public native void stopForwarder();

    public native boolean isRunningFacemgr();

    public native void startFacemgr();

    public native void stopFacemgr();

    public native void enableDiscovery(boolean enableDiscovery);

    public native void disableIPv4(boolean disableIPv4);

    public native void disableIPv6(boolean disableIPv6);

    public native void updateInterfaceIPv4(int interfaceType, int sourcePort, String nextHopIp, int nextHopPort);

    public native void updateInterfaceIPv6(int interfaceType, int sourcePort, String nextHopIp, int nextHopPort);

    public native void unsetInterfaceIPv4(int interfaceType);

    public native void unsetInterfaceIPv6(int interfaceType);

    public native String test();

}
