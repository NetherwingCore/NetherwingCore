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

    public static enum RealmFlags {
        REALM_FLAG_NONE(0x00),
        REALM_FLAG_VERSION_MISMATCH(0x01),
        REALM_FLAG_OFFLINE(0x02),
        REALM_FLAG_SPECIFYBUILD(0x04),
        REALM_FLAG_UNK1(0x08),
        REALM_FLAG_UNK2(0x10),
        REALM_FLAG_RECOMMENDED(0x20),
        REALM_FLAG_NEW(0x40),
        REALM_FLAG_FULL(0x80);

        private final int value;

        RealmFlags(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public static enum RealmType {

        REALM_TYPE_NORMAL(0),
        REALM_TYPE_PVP(1),
        REALM_TYPE_NORMAL2(4),
        REALM_TYPE_RP(6),
        REALM_TYPE_RPPVP(8),

        MAX_CLIENT_REALM_TYPE(14),

        REALM_TYPE_FFA_PVP(16);

        private final int value;

        RealmType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

}
