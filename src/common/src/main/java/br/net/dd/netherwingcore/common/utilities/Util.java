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

/**
 * Utility class providing various helper methods for commonly used operations.
 * This class includes methods for string manipulation, byte conversion, time formatting,
 * and several other utility functions relevant to most Java applications.
 */
public class Util {

    /**
     * Retrieves the location of the JAR file of the current application.
     *
     * @return The directory where the JAR file resides.
     * @throws RuntimeException If the URI syntax is invalid.
     */
    public static String getJarLocation() {
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile.getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Produces a system beep sound.
     */
    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return The hexadecimal representation of the byte array.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Tokenizes a string based on a given separator and keeps empty tokens if specified.
     *
     * @param str       The string to tokenize.
     * @param sep       The separator character.
     * @param keepEmpty Whether to include empty tokens in the result.
     * @return A list of tokens extracted from the input string.
     */
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

    /**
     * Compares two strings for equality, ignoring case considerations.
     *
     * @param a The first string to compare.
     * @param b The second string to compare.
     * @return {@code true} if the strings are equal ignoring case, {@code false} otherwise.
     */
    public static boolean stringEqualI(String a, String b) {
        return a.equalsIgnoreCase(b);
    }

    /**
     * Removes invisible characters (e.g., spaces, tabs, etc.) from a StringBuilder, leaving one space as a separator.
     *
     * @param str The StringBuilder to process.
     */
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

    /**
     * Rounds a float value based on a threshold of 0.44444445.
     *
     * @param val The float value to round.
     * @return The rounded integer value.
     */
    public static int roundingFloatValue(float val) {
        int intVal = (int) val;
        float difference = val - intVal;

        if (difference >= 0.44444445f) {
            intVal++;
        }

        return intVal;
    }

    /**
     * Converts seconds into a human-readable time string.
     *
     * @param timeInSecs The time in seconds.
     * @param shortText  Whether to use short text format (e.g., "d", "h").
     * @param hoursOnly  Whether to display hours only.
     * @return A formatted time string.
     */
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

    /**
     * Converts a UNIX timestamp to a formatted timestamp string.
     *
     * @param t The UNIX timestamp (in seconds).
     * @return The formatted timestamp string.
     */
    public static String timeToTimestampStr(long t) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochSecond(t));
    }

    /**
     * Checks if a given string is a valid IP address.
     *
     * @param ipAddress The string to validate.
     * @return {@code true} if the string is a valid IP address, {@code false} otherwise.
     */
    public static boolean isIPAddress(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves the current process ID (PID) of the running application.
     *
     * @return The process ID.
     */
    public static long getPID() {
        return ProcessHandle.current().pid();
    }

    /**
     * Calculates the number of UTF-8 code points in a given string.
     *
     * @param utf8str The input string.
     * @return The number of code points in the string.
     */
    public static int utf8Length(String utf8str) {
        return utf8str.codePointCount(0, utf8str.length());
    }

    /**
     * Truncates a StringBuilder to a specified number of UTF-8 code points.
     *
     * @param str The StringBuilder to truncate.
     * @param len The maximum number of UTF-8 code points to retain.
     */
    public static void utf8Truncate(StringBuilder str, int len) {
        int total = utf8Length(str.toString());
        if (total <= len) {
            return;
        }

        String truncated = str.substring(0, str.offsetByCodePoints(0, len));
        str.setLength(0);
        str.append(truncated);
    }

    /**
     * Converts an angle from degrees to radians.
     *
     * @param degrees The angle in degrees.
     * @return The angle in radians.
     */
    public static float degToRad(float degrees) {
        return (float) (degrees * (2.0 * Math.PI / 360.0));
    }

    /**
     * Converts a byte array to a hexadecimal string with optional reversal of the byte order.
     *
     * @param bytes   The byte array to convert.
     * @param reverse Whether to reverse the byte order.
     * @return The hexadecimal string.
     */
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

    /**
     * Converts a hexadecimal string to a byte array with optional reversal of the byte order.
     *
     * @param str     The hexadecimal string to convert.
     * @param reverse Whether to reverse the byte order.
     * @return The resulting byte array.
     * @throws IllegalArgumentException If the string has an odd number of characters.
     */
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

    /**
     * Converts a string to a boolean value, interpreting common representations of "true".
     *
     * @param str The string to convert (e.g., "1", "true", "yes").
     * @return {@code true} if the string represents a true value, {@code false} otherwise.
     */
    public static boolean stringToBool(String str) {
        String lowerStr = str.toLowerCase();
        return lowerStr.equals("1") || lowerStr.equals("true") || lowerStr.equals("yes");
    }

}
