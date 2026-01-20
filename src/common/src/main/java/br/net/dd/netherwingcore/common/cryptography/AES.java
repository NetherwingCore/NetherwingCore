package br.net.dd.netherwingcore.common.cryptography;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AES {

    public static final int IV_SIZE_BYTES = 12;
    public static final int KEY_SIZE_BYTES = 16;
    public static final int TAG_SIZE_BYTES = 12;

    private final boolean encrypting;
    private final Cipher cipher;
    private SecretKey secretKey;

    public AES(boolean encrypting, int keySizeBits) throws Exception {
        if (keySizeBits != 128 && keySizeBits != 192 && keySizeBits != 256) {
            throw new IllegalArgumentException("Invalid AES key size: " + keySizeBits);
        }
        this.encrypting = encrypting;
        this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
    }

    // Initialize with key
    public void init(byte[] key) {
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException("Key must be " + KEY_SIZE_BYTES + " bytes");
        }
        this.secretKey = new SecretKeySpec(key, "AES");
    }

    // Processes data with IV and returns ciphertext + tag.
    public byte[] process(byte[] iv, byte[] data) throws Exception {
        if (iv.length != IV_SIZE_BYTES) {
            throw new IllegalArgumentException("IV must be " + IV_SIZE_BYTES + " bytes");
        }

        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE_BYTES * 8, iv);
        if (encrypting) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        }

        return cipher.doFinal(data);
    }

    // Processes without integrity checking
    public byte[] processNoIntegrityCheck(byte[] iv, byte[] partialData) throws Exception {
        if (encrypting) {
            throw new IllegalStateException("Partial encryption is not allowed");
        }
        return process(iv, partialData);
    }

    // Generates random IV
    public static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

}



