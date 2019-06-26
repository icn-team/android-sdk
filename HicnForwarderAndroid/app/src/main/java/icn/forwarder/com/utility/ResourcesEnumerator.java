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

package icn.forwarder.com.utility;

public enum ResourcesEnumerator {
    SOURCE_IP("source_ip_key"),
    SOURCE_PORT("source_port_key"),
    NEXT_HOP_IP("next_hop_ip_key"),
    NEXT_HOP_PORT("next_hop_port_key"),
    CONFIGURATION("configuration_key"),
    SOURCE_NETWORK_INTERFACE("source_network_interface_key"),
    PREFIX("prefix_key"),
    NETMASK("netmask_key"),
    CAPACITY("capacity_key");

    private String key;

    ResourcesEnumerator(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
