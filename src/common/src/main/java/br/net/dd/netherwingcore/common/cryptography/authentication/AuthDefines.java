package br.net.dd.netherwingcore.common.cryptography.authentication;

import java.util.Arrays;

public class AuthDefines {

    public static final int SESSION_KEY_LENGTH = 40;

    // Define a class representing the session key as a fixed array of bytes.
    public static class SessionKey {

        private final byte[] key;

        public SessionKey() {
            this.key = new byte[SESSION_KEY_LENGTH];
        }

        public SessionKey(byte[] key) {
            if (key.length != SESSION_KEY_LENGTH) {
                throw new IllegalArgumentException("Key length should be " + SESSION_KEY_LENGTH + " bytes.");
            }
            this.key = Arrays.copyOf(key, SESSION_KEY_LENGTH);
        }

        public byte[] getKey() {
            return Arrays.copyOf(key, SESSION_KEY_LENGTH);
        }

        public void setKey(byte[] newKey) {
            if (newKey.length != SESSION_KEY_LENGTH) {
                throw new IllegalArgumentException("Key length should be " + SESSION_KEY_LENGTH + " bytes.");
            }
            System.arraycopy(newKey, 0, this.key, 0, SESSION_KEY_LENGTH);
        }
    }

}
