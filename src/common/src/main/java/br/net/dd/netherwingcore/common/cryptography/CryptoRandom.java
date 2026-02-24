package br.net.dd.netherwingcore.common.cryptography;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * Utility class for generating cryptographically secure random bytes.
 */
public final class CryptoRandom {

    // SecureRandom is thread-safe, so we can use a single instance for the entire application.
    private static final SecureRandom RNG = new SecureRandom();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CryptoRandom() {
        // util class
    }

    /**
     * Fills the provided byte array with random bytes.
     *
     * @param buf the byte array to fill with random bytes
     * @throws NullPointerException if buf is null
     */
    public static void getRandomBytes(byte[] buf) {
        Objects.requireNonNull(buf, "buf");
        RNG.nextBytes(buf);
    }

    /**
     * Fills the first len bytes of the provided byte array with random bytes.
     *
     * @param buf the byte array to fill with random bytes
     * @param len the number of random bytes to generate (must be in range [0, buf.length])
     * @throws NullPointerException if buf is null
     * @throws IllegalArgumentException if len is negative or greater than buf.length
     */
    public static void getRandomBytes(byte[] buf, int len) {
        Objects.requireNonNull(buf, "buf");
        if (len < 0 || len > buf.length)
            throw new IllegalArgumentException("len must be in range [0, buf.length]");

        if (len == buf.length) {
            RNG.nextBytes(buf);
            return;
        }

        // If len is less than buf.length, we generate random bytes into a temporary array and copy them to the beginning of buf.
        // This avoids modifying the remaining bytes in buf that are not part of the random data.
        byte[] tmp = new byte[len];
        RNG.nextBytes(tmp);
        System.arraycopy(tmp, 0, buf, 0, len);
    }

    /**
     * Generates a new byte array of the specified size filled with random bytes.
     *
     * @param size the size of the byte array to generate (must be >= 0)
     * @return a new byte array filled with random bytes
     * @throws IllegalArgumentException if size is negative
     */
    public static byte[] getRandomBytes(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size must be >= 0");

        byte[] arr = new byte[size];
        RNG.nextBytes(arr);
        return arr;
    }
}
