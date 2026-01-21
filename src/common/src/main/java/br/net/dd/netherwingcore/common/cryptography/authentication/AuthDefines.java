package br.net.dd.netherwingcore.common.cryptography.authentication;

import java.util.Arrays;

/**
 * Defines constants and utilities related to authentication within the NetherwingCore system.
 * This class provides a definition for the session key used in cryptographic operations and ensures
 * its validity and consistency by enforcing its fixed length.
 */
public class AuthDefines {

    /**
     * The fixed length of the session key in bytes.
     */
    public static final int SESSION_KEY_LENGTH = 40;

    /**
     * Represents a session key with a fixed length of 40 bytes.
     * Provides methods to create, retrieve, and update the session key while ensuring its validity.
     */
    public static class SessionKey {

        private final byte[] key;

        /**
         * Default constructor that initializes the session key with an empty byte array
         * of fixed length {@link AuthDefines#SESSION_KEY_LENGTH}.
         */
        public SessionKey() {
            this.key = new byte[SESSION_KEY_LENGTH];
        }

        /**
         * Constructs a session key with the given byte array. The array must have a length of
         * {@link AuthDefines#SESSION_KEY_LENGTH}.
         *
         * @param key The byte array representing the session key.
         * @throws IllegalArgumentException If the length of the byte array does not match {@link AuthDefines#SESSION_KEY_LENGTH}.
         */
        public SessionKey(byte[] key) {
            if (key.length != SESSION_KEY_LENGTH) {
                throw new IllegalArgumentException("Key length should be " + SESSION_KEY_LENGTH + " bytes.");
            }
            this.key = Arrays.copyOf(key, SESSION_KEY_LENGTH);
        }

        /**
         * Retrieves a copy of the session key.
         * Protects the internal representation of the key by returning a copy.
         *
         * @return A copy of the session key as a byte array.
         */
        public byte[] getKey() {
            return Arrays.copyOf(key, SESSION_KEY_LENGTH);
        }

        /**
         * Updates the current session key with a new byte array.
         * The new byte array must have a length of {@link AuthDefines#SESSION_KEY_LENGTH}.
         *
         * @param newKey The new byte array representing the session key.
         * @throws IllegalArgumentException If the length of the byte array does not match {@link AuthDefines#SESSION_KEY_LENGTH}.
         */
        public void setKey(byte[] newKey) {
            if (newKey.length != SESSION_KEY_LENGTH) {
                throw new IllegalArgumentException("Key length should be " + SESSION_KEY_LENGTH + " bytes.");
            }
            System.arraycopy(newKey, 0, this.key, 0, SESSION_KEY_LENGTH);
        }
    }

}
