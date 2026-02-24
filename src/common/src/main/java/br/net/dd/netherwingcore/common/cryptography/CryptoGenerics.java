package br.net.dd.netherwingcore.common.cryptography;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Generic cryptographic utilities, not specific to any particular use case.
 */
public final class CryptoGenerics {

    private static final SecureRandom RNG = new SecureRandom();

    /**
     * No instantiation, just static methods.
     */
    private CryptoGenerics() {}

    /**
     * Generates a random IV of the specified length in bytes.
     * @param ivLenBytes The length of the IV in bytes (e.g., 12 for AES-GCM).
     * @return A byte array containing the random IV.
     */
    public static byte[] generateRandomIV(int ivLenBytes) {
        byte[] iv = new byte[ivLenBytes];
        RNG.nextBytes(iv);
        return iv;
    }

    /**
     * Appends the given tail byte array to the back of the data byte array.
     * @param data The original byte array.
     * @param tail The byte array to append to the back of data.
     * @return A new byte array containing data followed by tail.
     */
    public static byte[] appendToBack(byte[] data, byte[] tail) {
        byte[] out = Arrays.copyOf(data, data.length + tail.length);
        System.arraycopy(tail, 0, out, data.length, tail.length);
        return out;
    }

    /**
     * Encrypts the given plaintext using AES-GCM with a random IV. The output format is:
     * [ciphertext][IV][tag]
     * @param plaintext The plaintext to encrypt.
     * @param key The secret key for encryption.
     * @param ivLenBytes The length of the IV in bytes (e.g., 12 for AES-GCM).
     * @param tagLenBytes The length of the authentication tag in bytes (e.g., 16 for 128-bit tag).
     * @return A byte array containing the ciphertext followed by the IV and then the tag.
     * @throws GeneralSecurityException If encryption fails or if the ciphertext is shorter than the tag.
     */
    public static byte[] aeEncryptWithRandomIV_AesGcm(
            byte[] plaintext,
            SecretKey key,
            int ivLenBytes,      // ex.: 12
            int tagLenBytes      // ex.: 16 (128 bits)
    ) throws GeneralSecurityException {

        byte[] iv = generateRandomIV(ivLenBytes);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(tagLenBytes * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ctAndTag = cipher.doFinal(plaintext);

        // ctAndTag is [ciphertext][tag], we want to split it and rearrange to [ciphertext][IV][tag]
        if (ctAndTag.length < tagLenBytes)
            throw new GeneralSecurityException("Ciphertext shorter than tag");

        int ctLen = ctAndTag.length - tagLenBytes;
        byte[] ciphertext = Arrays.copyOfRange(ctAndTag, 0, ctLen);
        byte[] tag = Arrays.copyOfRange(ctAndTag, ctLen, ctAndTag.length);

        byte[] out = appendToBack(ciphertext, iv);
        out = appendToBack(out, tag);
        return out;
    }

    /**
     * Decrypts the given data using AES-GCM. The input format is expected to be:
     * [ciphertext][IV][tag]
     * @param data The data to decrypt, containing the ciphertext followed by the IV and then the tag.
     * @param key The secret key for decryption.
     * @param ivLenBytes The length of the IV in bytes (e.g., 12 for AES-GCM).
     * @param tagLenBytes The length of the authentication tag in bytes (e.g., 16 for 128-bit tag).
     * @return A byte array containing the decrypted plaintext.
     * @throws GeneralSecurityException If decryption fails or if the input data is too short to contain the IV and tag.
     */
    public static byte[] aeDecrypt_AesGcm(
            byte[] data,
            SecretKey key,
            int ivLenBytes,
            int tagLenBytes
    ) throws GeneralSecurityException {

        if (data.length < ivLenBytes + tagLenBytes)
            throw new GeneralSecurityException("Input too short for IV+TAG");

        int tagStart = data.length - tagLenBytes;
        int ivStart = tagStart - ivLenBytes;

        byte[] ciphertext = Arrays.copyOfRange(data, 0, ivStart);
        byte[] iv = Arrays.copyOfRange(data, ivStart, tagStart);
        byte[] tag = Arrays.copyOfRange(data, tagStart, data.length);

        // Reconstruct the original [ciphertext][tag] format for decryption
        byte[] ctAndTag = appendToBack(ciphertext, tag);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(tagLenBytes * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        // This will throw AEADBadTagException if authentication fails
        return cipher.doFinal(ctAndTag);
    }
}
