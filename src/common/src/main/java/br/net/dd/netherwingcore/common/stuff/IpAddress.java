package br.net.dd.netherwingcore.common.stuff;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddress {

    public static InetAddress makeAddress(String addressStr) throws UnknownHostException {
        return InetAddress.getByName(addressStr);
    }

    public static InetAddress makeAddress(String addressStr, StringBuilder errorCode) {
        try {
            return InetAddress.getByName(addressStr);
        } catch (UnknownHostException e) {
            if (errorCode != null) {
                errorCode.append(e.getMessage());
            }
            return null;
        }
    }

    public static Inet4Address makeAddressV4(String addressStr) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(addressStr);
        if (address instanceof Inet4Address) {
            return (Inet4Address) address;
        } else {
            throw new UnknownHostException("Not an IPv4 address: " + addressStr);
        }
    }

    public static Inet4Address makeAddressV4(String addressStr, StringBuilder errorCode) {
        try {
            InetAddress address = InetAddress.getByName(addressStr);
            if (address instanceof Inet4Address) {
                return (Inet4Address) address;
            } else {
                throw new UnknownHostException("Not an IPv4 address: " + addressStr);
            }
        } catch (UnknownHostException e) {
            if (errorCode != null) {
                errorCode.append(e.getMessage());
            }
            return null;
        }
    }

    public static long addressToUInt(Inet4Address address) {
        byte[] bytes = address.getAddress();
        return ((bytes[0] & 0xFFL) << 24) |
                ((bytes[1] & 0xFFL) << 16) |
                ((bytes[2] & 0xFFL) << 8) |
                (bytes[3] & 0xFFL);
    }

}
