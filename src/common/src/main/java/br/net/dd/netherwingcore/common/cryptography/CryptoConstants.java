package br.net.dd.netherwingcore.common.cryptography;

/**
 * A utility class that defines constants related to cryptographic operations, such as digest lengths for various hashing algorithms.
 * This class is not meant to be instantiated and serves as a centralized location for cryptographic constants used throughout the application.
 */
public final class CryptoConstants {

    /**
     * This class contains constants related to cryptographic operations, such as digest lengths for various hashing algorithms.
     * It is designed to be a utility class and should not be instantiated.
     */
    private CryptoConstants() {
        // prevent instantiation
    }

    public static final int MD5_DIGEST_LENGTH_BYTES = 16;
    public static final int SHA1_DIGEST_LENGTH_BYTES = 20;
    public static final int SHA256_DIGEST_LENGTH_BYTES = 32;
    public static final int SHA512_DIGEST_LENGTH_BYTES = 64;

}