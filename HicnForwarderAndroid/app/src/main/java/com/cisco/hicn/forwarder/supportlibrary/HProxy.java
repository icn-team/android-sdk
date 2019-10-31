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

import java.util.concurrent.atomic.AtomicInteger;

public class HProxy {
    private static HProxy sInstance = null;
    private AtomicInteger mNextConnectorId = new AtomicInteger(1);

    private int mVpnConnector = -1;
    private int mUdpTunnelConnector = -1;
    private int mIcnConnector = -1;

    static {
        System.loadLibrary("hproxy-wrapper");
    }

    public static HProxy getInstance() {
        if (sInstance == null) {
            sInstance = new HProxy();
        }
        return sInstance;
    }

    private HProxy() {
        initConfig();
    }

    public native void initConfig();

    public native boolean isRunning();

    public native void start();

    public native void stop();

    public void setIcnConnector(String consumerName, String producerName) {
        mIcnConnector = mNextConnectorId.getAndIncrement();
        addIcnConnectorInternal(mIcnConnector, consumerName, producerName);
    }

    public void setUdpTunnelConnector(String remoteAddress, String remotePort) {
        mUdpTunnelConnector = mNextConnectorId.getAndIncrement();
        addUdpTunnelConnectorInternal(mUdpTunnelConnector, remoteAddress, remotePort);
    }

    public void setVpnConnector(int fd) {
        mVpnConnector = mNextConnectorId.getAndIncrement();
        addVpnConnectorInternal(mVpnConnector, fd);
    }

    public void linkConnectors() {
        // ICN to VPN
        linkConnectors(mIcnConnector, mVpnConnector);

        // UDP to VPN
        linkConnectors(mUdpTunnelConnector, mVpnConnector);

        // VPN to ICN/UDP (UDP is done internally)
        // TODO make connection to UDP explicit
        linkConnectors(mVpnConnector, mIcnConnector);
    }

    private void linkConnectors(int connectorId0, int connectorId1) {
        if (connectorId0 == mVpnConnector) {
            linkTunToConnector(connectorId0, connectorId1);
        } else {
            linkConnectorsInternal(connectorId0, connectorId1);
        }
    }

    private native void linkConnectorsInternal(int connectorId0, int connectorId1);

    private native void linkTunToConnector(int connectorId0, int connectorId1);

    private native void removeConnectorInternal(int connectorId);

    private native int addIcnConnectorInternal(int connectorId, String consumerName, String producerName);

    private native int addUdpTunnelConnectorInternal(int connectorId, String remoteAddress, String remotePort);

    private native int addVpnConnectorInternal(int connectorId, int fd);

    public native boolean isHProxyEnabled();
}
