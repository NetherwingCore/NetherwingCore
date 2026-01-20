package br.net.dd.netherwingcore.common.cryptography;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class AES {
    private Cipher cipher;
    private boolean encrypting;
    private SecretKey secretKey; // We store the key used to initialize Cipher.

    // We store the key used to initialize the CipherConstructor: It supports key sizes of 128, 192, and 256 bits.
    public AES(boolean encrypting, int keySizeBits) throws Exception {
        this.encrypting = encrypting;

        if (keySizeBits != 128 && keySizeBits != 192 && keySizeBits != 256) {
            throw new IllegalArgumentException("Invalid AES key size: " + keySizeBits);
        }

        this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
    }

    // Method for generating an AES key
    public static SecretKey generateKey(int keySizeBits) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySizeBits, new SecureRandom());
        return keyGenerator.generateKey();
    }

    // Initialize with a provided key
    public void init(SecretKey key) throws Exception {
        this.secretKey = key; // Store the key
        cipher.init(encrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key);
    }

    public void init(byte[] keyBytes) throws Exception {
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        init(key); // Call the method above for reuse.
    }

    // Process data with integrity verification (GCM default mode)
    public boolean process(byte[] iv, byte[] data, int length, byte[] tag) throws Exception {
        if (iv == null || iv.length != 12) { // GCM recommends a 12-byte IV.
            throw new IllegalArgumentException("IV must be 12 bytes long");
        }

        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Data length exceeds maximum size");
        }

        GCMParameterSpec gcmSpec = new GCMParameterSpec(tag.length * 8, iv);

        cipher.init(encrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] output = cipher.doFinal(data);
        System.arraycopy(output, 0, data, 0, output.length); // Sobrescreve o buffer de entrada com o resultado

        if (!encrypting && !Arrays.equals(tag, cipher.getIV())) {
            throw new SecurityException("Integrity check failed");
        }

        return true;
    }

    // Process data without integrity verification (Decryption only)
    public boolean processNoIntegrityCheck(byte[] iv, byte[] data, int partialLength) throws Exception {
        if (encrypting) {
            throw new IllegalArgumentException("Partial encryption is not allowed");
        }

        if (partialLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Data length exceeds maximum size for Java");
        }

        if (iv == null || iv.length != 12) { // GCM recomenda IV de 12 bytes
            throw new IllegalArgumentException("IV must be 12 bytes long");
        }

        // Configuring GCMParameterSpec without authentication
        GCMParameterSpec gcmSpec = new GCMParameterSpec(0, iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        int offset = 0;
        int blockSize = cipher.getBlockSize();
        byte[] outBuffer = new byte[blockSize]; // Buffer de saída parcial
        byte[] partialBuffer = Arrays.copyOf(data, partialLength); // Dados parciais

        while (offset < partialBuffer.length) {
            int len = Math.min(blockSize, partialBuffer.length - offset);
            int updateBytes = cipher.update(partialBuffer, offset, len, outBuffer);
            if (updateBytes > 0) {
                System.arraycopy(outBuffer, 0, data, offset, updateBytes);
            }
            offset += len;
        }

        return true;
    }
}
