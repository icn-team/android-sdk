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
        System.loadLibrary("forwarderWrap");
    }

    public static NativeAccess getInstance() {
        if (sInstance == null) {
            sInstance = new NativeAccess();
        }
        return sInstance;
    }

    private NativeAccess() {

    }

    public native boolean isRunningForwarder();

    public native void startForwarder(int capacity);

    public native void stopForwarder();


    public native boolean isRunningFacemgr();

    public native void startFacemgr();

    public native void startFacemgrWithConfig(String nextHopIpV4Wifi, int nextHopPortIpV4Wifi,
                                              String nextHopIpV6Wifi, int nextHopPortIpV6Wifi,
                                              String nextHopIpV4Radio, int nextHopPortIpV4Radio,
                                              String nextHopIpV6Radio, int nextHopPortIpV6Radio,
                                              String nextHopIpV4Wired, int nextHopPortIpV4Wired,
                                              String nextHopIpV6Wired, int nextHopPortIpV6Wired);

    public native void stopFacemgr();

}
