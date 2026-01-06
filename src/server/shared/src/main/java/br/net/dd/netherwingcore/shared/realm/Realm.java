package br.net.dd.netherwingcore.shared.realm;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Realm {

    private String name;
    private String normalizedName;
    private InetAddress localAddress;
    private InetAddress externalAddress;
    private InetAddress localSubnetMask;
    private int type;
    private static final int[] CONFIG_ID_BY_TYPE = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

    public void setName(String name) {
        this.name = name;
        this.normalizedName = name.replaceAll("\\s", ""); // Remove blank spaces
    }

    public InetAddress getAddressForClient(InetAddress clientAddr) throws UnknownHostException {
        InetAddress realmIp;

        // Try to send the best address to the customer.
        if (clientAddr.isLoopbackAddress()) {
            // Check if the realm is also connected locally.
            if (localAddress.isLoopbackAddress() || externalAddress.isLoopbackAddress()) {
                realmIp = clientAddr;
            } else {
                // Assume that the client connects from the same machine as the bnet server.
                realmIp = localAddress;
            }
        } else {
            if (clientAddr instanceof java.net.Inet4Address &&
                    TrinityNet.isInNetwork(localAddress, localSubnetMask, clientAddr)) {
                realmIp = localAddress;
            } else {
                realmIp = externalAddress;
            }
        }

        return realmIp;
    }

    public int getConfigId() {
        return CONFIG_ID_BY_TYPE[type];
    }

    // Auxiliary class "TrinityNet"
    public static class TrinityNet {

        public static boolean isInNetwork(InetAddress localAddress, InetAddress subnetMask, InetAddress clientAddress) {
            // Custom implementation to check if the client's address is on the local network.
            byte[] localBytes = localAddress.getAddress();
            byte[] maskBytes = subnetMask.getAddress();
            byte[] clientBytes = clientAddress.getAddress();

            for (int i = 0; i < localBytes.length; i++) {
                if ((localBytes[i] & maskBytes[i]) != (clientBytes[i] & maskBytes[i])) {
                    return false;
                }
            }
            return true;
        }

    }

    public static class Battlenet {

        public static class RealmHandle {
            private int region;
            private int site;
            private int realm; // primary key in `realmlist` table

            public RealmHandle(int region, int site, int realm) {
                this.region = region;
                this.site = site;
                this.realm = realm;
            }

            public String getAddressString() {
                return String.format("%d-%d-%d", region, site, realm);
            }

            public String getSubRegionAddress() {
                return String.format("%d-%d-0", region, site);
            }
        }
    }

}
