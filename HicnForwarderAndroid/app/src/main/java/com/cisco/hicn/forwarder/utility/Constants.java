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
package com.cisco.hicn.forwarder.utility;

public class Constants {

    public static final String ENABLED = "Enabled";
    public static final String DISABLED = "Disabled";
    public static final String FORWARDER_PREFERENCES = "forwarderPreferences";
    public static final String DEFAULT_CAPACITY = "0";

    public static final int FOREGROUND_SERVICE = 101;

    public static final int AU_INTERFACE_TYPE_UNDEFINED = 0;
    public static final int AU_INTERFACE_TYPE_WIRED = 1;
    public static final int AU_INTERFACE_TYPE_WIFI = 2;
    public static final int AU_INTERFACE_TYPE_CELLULAR = 3;
    public static final int AU_INTERFACE_TYPE_LOOPBACK = 4;
    public static final String HIPERF = "HIPERF";
    public static final int VIEW_FORWARDER = 0;
    public static final int VIEW_INTERFACES = 1;

    public static int MAX_HIPING_TIME_LINECHART_XAXIS = 30;
}
