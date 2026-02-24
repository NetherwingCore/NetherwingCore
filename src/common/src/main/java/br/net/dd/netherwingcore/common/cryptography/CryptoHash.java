package br.net.dd.netherwingcore.common.cryptography;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for cryptographic hashing using Java's MessageDigest.
 * Provides a flexible interface for creating and using various hash algorithms.
 */
public final class CryptoHash {

    /**
     * Functional interface for creating MessageDigest instances.
     */
    @FunctionalInterface
    public interface DigestCreator {
        MessageDigest create();
    }

    /**
     * GenericHash class that wraps a MessageDigest instance and provides a convenient API for hashing data.
     */
    public static final class GenericHash {
        private final DigestCreator creator;
        private final int digestLength;

        private MessageDigest md;
        private byte[] digest;

        /**
         * Constructs a GenericHash with the specified DigestCreator and digest length.
         *
         * @param creator      the DigestCreator to create MessageDigest instances
         * @param digestLength the expected length of the digest output
         */
        public GenericHash(DigestCreator creator, int digestLength) {
            this.creator = Objects.requireNonNull(creator, "creator");
            this.digestLength = digestLength;
            this.md = creator.create();
            this.digest = new byte[digestLength];
        }

        /**
         * Creates a copy of this GenericHash, including its current state.
         *
         * @return a new GenericHash instance with the same state
         */
        public GenericHash copy() {
            GenericHash h = new GenericHash(this.creator, this.digestLength);
            h.md = cloneDigest(this.md);
            h.digest = Arrays.copyOf(this.digest, this.digest.length);
            return h;
        }

        /**
         * Updates the hash with the specified byte array.
         *
         * @param data the data to update the hash with
         * @return this GenericHash instance for chaining
         */
        public GenericHash update(byte[] data) {
            Objects.requireNonNull(data, "data");
            md.update(data);
            return this;
        }

        /**
         * Updates the hash with a portion of the specified byte array.
         *
         * @param data   the data to update the hash with
         * @param offset the starting offset in the data
         * @param len    the number of bytes to use from the data
         * @return this GenericHash instance for chaining
         */
        public GenericHash update(byte[] data, int offset, int len) {
            Objects.requireNonNull(data, "data");
            md.update(data, offset, len);
            return this;
        }

        /**
         * Updates the hash with the specified string, using UTF-8 encoding.
         *
         * @param s the string to update the hash with
         * @return this GenericHash instance for chaining
         */
        public GenericHash update(String s) {
            Objects.requireNonNull(s, "s");
            md.update(s.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        /**
         * Finalizes the digest computation and resets the internal state for reuse.
         *
         * @return this GenericHash instance for chaining
         */
        public GenericHash finalizeDigest() {
            byte[] out = md.digest(); // This also resets the MessageDigest state, so we can reuse it for the next computation
            if (out.length != digestLength) {
                throw new IllegalStateException(
                        "Digest length mismatch: got " + out.length + " expected " + digestLength);
            }
            this.digest = out;
            this.md = creator.create();
            return this;
        }

        /**
         * Returns a copy of the computed digest.
         *
         * @return a byte array containing the digest
         */
        public byte[] getDigest() {
            return Arrays.copyOf(digest, digest.length);
        }

        /**
         * Static utility method to compute the digest of the given data using the specified DigestCreator and digest length.
         *
         * @param creator      the DigestCreator to create MessageDigest instances
         * @param digestLength the expected length of the digest output
         * @param data         the data to compute the digest of
         * @return a byte array containing the computed digest
         */
        public static byte[] getDigestOf(DigestCreator creator, int digestLength, byte[] data) {
            return new GenericHash(creator, digestLength)
                    .update(data)
                    .finalizeDigest()
                    .getDigest();
        }

        /**
         * Static utility method to compute the digest of the given string using the specified DigestCreator and digest length.
         *
         * @param creator      the DigestCreator to create MessageDigest instances
         * @param digestLength the expected length of the digest output
         * @param parts        the strings to compute the digest of
         * @return a byte array containing the computed digest
         */
        public static byte[] getDigestOf(DigestCreator creator, int digestLength, Object... parts) {
            GenericHash h = new GenericHash(creator, digestLength);
            for (Object p : parts) {
                if (p == null) continue;
                if (p instanceof byte[] b) {
                    h.update(b);
                } else if (p instanceof String s) {
                    h.update(s);
                } else {
                    throw new IllegalArgumentException("Unsupported part type: " + p.getClass().getName());
                }
            }
            return h.finalizeDigest().getDigest();
        }

        /**
         * Utility method to clone a MessageDigest instance.
         *
         * @param md the MessageDigest instance to clone
         * @return a cloned MessageDigest instance
         * @throws UnsupportedOperationException if the MessageDigest cannot be cloned
         */
        private static MessageDigest cloneDigest(MessageDigest md) {
            try {
                return (MessageDigest) md.clone();
            } catch (CloneNotSupportedException e) {
                // This should not happen for standard MessageDigest implementations, but we handle it just in case
                throw new UnsupportedOperationException("MessageDigest cannot be cloned for " + md.getAlgorithm(), e);
            }
        }
    }

    /**
     * Predefined DigestCreators and their corresponding digest lengths for common algorithms.
     */
    public static final class Algorithms {
        public static final DigestCreator MD5 = () -> get("MD5");
        public static final DigestCreator SHA1 = () -> get("SHA-1");
        public static final DigestCreator SHA256 = () -> get("SHA-256");
        public static final DigestCreator SHA512 = () -> get("SHA-512");

        public static final int MD5_LEN = 16;
        public static final int SHA1_LEN = 20;
        public static final int SHA256_LEN = 32;
        public static final int SHA512_LEN = 64;

        /**
         * Utility method to get a MessageDigest instance for the specified algorithm.
         *
         * @param algo the name of the algorithm (e.g., "MD5", "SHA-1", "SHA-256", "SHA-512")
         * @return a MessageDigest instance for the specified algorithm
         */
        private static MessageDigest get(String algo) {
            try {
                return MessageDigest.getInstance(algo);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Algorithm unavailable.: " + algo, e);
            }
        }
    }
}
