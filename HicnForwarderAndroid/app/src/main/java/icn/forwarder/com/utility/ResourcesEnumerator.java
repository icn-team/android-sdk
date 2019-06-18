package icn.forwarder.com.utility;

/**
 * Created by angelomantellini on 18/05/2017.
 */

public enum ResourcesEnumerator {
    SOURCE_IP("sourceIp"),
    SOURCE_PORT("sourcePort"),
    NEXT_HOP_IP("nextHopIp"),
    NEXT_HOP_PORT("nextHopPort"),
    CONFIGURATION("configuration"),
    SOURCE_NETWORK_INTERFACE("sourceNetworkInterface"),
    PREFIX("prefix"),
    NETMASK("netmask");

    private String key;

    ResourcesEnumerator(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
