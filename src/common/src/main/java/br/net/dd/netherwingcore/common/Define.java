package br.net.dd.netherwingcore.common;

import java.nio.ByteOrder;

/**
 * Core definitions and constants for NetherwingCore.
 */
public final class Define {

    // Prevent instantiation
    private Define() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // Endianness constants
    public static final int NETHERWING_LITTLEENDIAN = 0;
    public static final int NETHERWING_BIGENDIAN = 1;

    // Determine system endianness at runtime
    public static final int NETHERWING_ENDIAN =
            ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)
                    ? NETHERWING_BIGENDIAN
                    : NETHERWING_LITTLEENDIAN;

    // Path constants
    public static final int NETHERWING_PATH_MAX = getPathMax();

    private static int getPathMax() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return 260;
        } else {
            return 4096; // PATH_MAX on most Unix systems
        }
    }

    // Debug flag (can be set via system property or build configuration)
    public static final boolean NETHERWING_DEBUG =
            System.getProperty("trinity.debug") != null ||
                    java.lang.management.ManagementFactory.getRuntimeMXBean()
                            .getInputArguments().toString().contains("-agentlib:jdwp");

    // Format string constants
    public static final String UI64FMTD = "%d";
    public static final String SI64FMTD = "%d";
    public static final String SZFMTD = "%d";
    public static final String STRING_VIEW_FMT = "%s";

    /**
     * Helper method for string view formatting
     * In C++: STRING_VIEW_FMT_ARG(str) -> static_cast<int>((str).length()), (str).data()
     * In Java: Just use the string directly with %s
     */
    public static String formatStringView(String str) {
        return str;
    }

    /**
     * Helper method to create string view format arguments
     * @param str the string to format
     * @return formatted string
     */
    public static String stringViewFmtArg(String str) {
        return String.format(STRING_VIEW_FMT, str);
    }

    // Type aliases (Java primitives - these are just for documentation)
    // In Java, we use the native types directly:
    // long (64-bit signed) = int64
    // int (32-bit signed) = int32
    // short (16-bit signed) = int16
    // byte (8-bit signed) = int8
    // For unsigned, we need to be careful with operations

    /**
     * DBC Format Types enumeration
     */
    public enum DBCFormer {
        FT_STRING('s'),                     // LocalizedString*
        FT_STRING_NOT_LOCALIZED('S'),       // char*
        FT_FLOAT('f'),                      // float
        FT_INT('i'),                        // uint32
        FT_BYTE('b'),                       // uint8
        FT_SHORT('h'),                      // uint16
        FT_LONG('l');                       // uint64

        private final char value;

        DBCFormer(char value) {
            this.value = value;
        }

        public char getValue() {
            return value;
        }

        public static DBCFormer fromChar(char c) {
            for (DBCFormer former : values()) {
                if (former.value == c) {
                    return former;
                }
            }
            throw new IllegalArgumentException("Unknown DBCFormer: " + c);
        }
    }

    // API visibility markers (in Java, these would be handled by access modifiers)
    // Just keeping as documentation/marker interfaces

    /**
     * Marker for Common API exports
     */
    public @interface CommonAPI {}

    /**
     * Marker for Proto API exports
     */
    public @interface ProtoAPI {}

    /**
     * Marker for Database API exports
     */
    public @interface DatabaseAPI {}

    /**
     * Marker for Network API exports
     */
    public @interface NetworkAPI {}

    /**
     * Marker for Shared API exports
     */
    public @interface SharedAPI {}

    /**
     * Marker for Game API exports
     */
    public @interface GameAPI {}

    /**
     * Marker for MMaps Common API exports
     */
    public @interface MMapsCommonAPI {}

    // Utility methods for unsigned integer operations

    /**
     * Convert signed long to unsigned string representation
     */
    public static String toUnsignedString(long value) {
        return Long.toUnsignedString(value);
    }

    /**
     * Convert signed int to unsigned long
     */
    public static long toUnsignedLong(int value) {
        return Integer.toUnsignedLong(value);
    }

    /**
     * Convert signed short to unsigned int
     */
    public static int toUnsignedInt(short value) {
        return Short.toUnsignedInt(value);
    }

    /**
     * Convert signed byte to unsigned int
     */
    public static int toUnsignedInt(byte value) {
        return Byte.toUnsignedInt(value);
    }
}
