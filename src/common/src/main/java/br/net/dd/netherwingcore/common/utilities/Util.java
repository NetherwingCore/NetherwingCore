package br.net.dd.netherwingcore.common.utilities;

import br.net.dd.netherwingcore.common.Main;

import java.awt.Toolkit;
import java.io.File;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static String getJarLocation() {
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile.getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static List<String> tokenize(String str, char sep, boolean keepEmpty) {
        List<String> tokens = new ArrayList<>();
        int start = 0;
        int end = str.indexOf(sep);

        while (end != -1) {
            if (keepEmpty || start < end) {
                tokens.add(str.substring(start, end));
            }
            start = end + 1;
            end = str.indexOf(sep, start);
        }

        if (keepEmpty || start < str.length()) {
            tokens.add(str.substring(start));
        }

        return tokens;
    }

    public static boolean stringEqualI(String a, String b) {
        return a.equalsIgnoreCase(b);
    }

    public static void stripLineInvisibleChars(StringBuilder str) {
        String invChars = " \t\7\n";
        int wpos = 0;
        boolean space = false;

        for (int pos = 0; pos < str.length(); pos++) {
            if (invChars.indexOf(str.charAt(pos)) != -1) {
                if (!space) {
                    str.setCharAt(wpos++, ' ');
                    space = true;
                }
            } else {
                str.setCharAt(wpos++, str.charAt(pos));
                space = false;
            }
        }

        if (wpos < str.length()) {
            str.setLength(wpos);
        }
    }

    public static int roundingFloatValue(float val) {
        int intVal = (int) val;
        float difference = val - intVal;

        if (difference >= 0.44444445f) {
            intVal++;
        }

        return intVal;
    }

    public static String secsToTimeString(long timeInSecs, boolean shortText, boolean hoursOnly) {
        long secs = timeInSecs % 60;
        long minutes = timeInSecs % 3600 / 60;
        long hours = timeInSecs % 86400 / 3600;
        long days = timeInSecs / 86400;

        StringBuilder ss = new StringBuilder();
        if (days > 0) {
            ss.append(days).append(shortText ? "d" : " Day(s) ");
        }
        if (hours > 0 || hoursOnly) {
            ss.append(hours).append(shortText ? "h" : " Hour(s) ");
        }
        if (!hoursOnly) {
            if (minutes > 0) {
                ss.append(minutes).append(shortText ? "m" : " Minute(s) ");
            }
            if (secs > 0 || (days == 0 && hours == 0 && minutes == 0)) {
                ss.append(secs).append(shortText ? "s" : " Second(s).");
            }
        }
        return ss.toString();
    }

    public static String timeToTimestampStr(long t) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochSecond(t));
    }

    public static boolean isIPAddress(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static long getPID() {
        return ProcessHandle.current().pid();
    }

    public static int utf8Length(String utf8str) {
        return utf8str.codePointCount(0, utf8str.length());
    }

    public static void utf8Truncate(StringBuilder str, int len) {
        int total = utf8Length(str.toString());
        if (total <= len) {
            return;
        }

        String truncated = str.substring(0, str.offsetByCodePoints(0, len));
        str.setLength(0);
        str.append(truncated);
    }

    public static float degToRad(float degrees) {
        return (float) (degrees * (2.0 * Math.PI / 360.0));
    }

    public static String byteArrayToHexStr(byte[] bytes, boolean reverse) {
        StringBuilder result = new StringBuilder();
        if (reverse) {
            for (int i = bytes.length - 1; i >= 0; i--) {
                result.append(String.format("%02X", bytes[i]));
            }
        } else {
            for (byte b : bytes) {
                result.append(String.format("%02X", b));
            }
        }
        return result.toString();
    }

    public static byte[] hexStrToByteArray(String str, boolean reverse) {
        if ((str.length() % 2) != 0) {
            throw new IllegalArgumentException("Hex string must have an even number of characters");
        }

        int len = str.length();
        byte[] result = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int index = reverse ? len - i - 2 : i;
            result[i / 2] = (byte) ((Character.digit(str.charAt(index), 16) << 4)
                    + Character.digit(str.charAt(index + 1), 16));
        }

        return result;
    }

    public static boolean stringToBool(String str) {
        String lowerStr = str.toLowerCase();
        return lowerStr.equals("1") || lowerStr.equals("true") || lowerStr.equals("yes");
    }

}
