package br.net.dd.netherwingcore.common.cryptography;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Java rewrite of Trinity::Crypto::ARC4 based on OpenSSL EVP.
 * <p>
 * - init(seed, len): sets the key (seed) and initializes the RC4 cipher.
 * - updateData(data, len): applies RC4 "in-place" on the buffer.
 * <p>
 * Note: RC4 is considered a weak cipher by modern standards and should not be used for secure applications.
 * This implementation is provided for compatibility with existing code that uses RC4, but it is recommended to use stronger algorithms (like AES) for new development.
 *
 */
public final class ARC4 implements AutoCloseable {
    private Cipher cipher;
    private boolean initialized;

    /**
     * Constructor initializes the Cipher instance for ARC4.
     * The actual key is set in the init() method, allowing for dynamic key lengths.
     */
    public ARC4() {
        try {
            // On some platforms you may need "RC4" instead of "ARCFOUR"
            // or the other way around. "ARCFOUR" is a common alias in Java providers.
            this.cipher = Cipher.getInstance("ARCFOUR");
            this.initialized = false;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to acquire ARCFOUR/RC4 Cipher instance", e);
        }
    }

    /**
     * Equivalent to Init(uint8* seed, size_t len) where 'seed' is the key and 'len' is the key length.
     * For RC4, the key can be of variable length (commonly between 1 and 256 bytes).
     */
    public void init(byte[] seed, int len) {
        if (seed == null)
            throw new IllegalArgumentException("seed == null");
        if (len < 0 || len > seed.length)
            throw new IllegalArgumentException("Invalid len: " + len);

        // OpenSSL allows setting key length dynamically; here we copy exactly 'len' bytes.
        byte[] keyBytes = Arrays.copyOf(seed, len);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "ARCFOUR");

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            initialized = true;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize ARC4", e);
        } finally {
            // Best practice: reduce exposure of temporary key material
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    /**
     * Equivalent to UpdateData(uint8* data, size_t len) performing "in-place" processing.
     * For RC4, encryption/decryption is the same operation (stream cipher).
     * <p>
     * Note: The method modifies the input 'data' array directly, similar to how the C++ code operates on the same buffer for input and output.
     * The OpenSSL code calls EVP_EncryptUpdate with the same buffer for input and output, and then calls EVP_EncryptFinal_ex (which does not add any bytes for RC4).
     * In Java, we use cipher.update() for the main processing and then call doFinal() to finalize the cipher state. For RC4, doFinal() should not produce additional output, but it is called to maintain consistency with the OpenSSL flow.
     *
     * @param data The input data to be encrypted/decrypted in-place.
     * @param len The number of bytes from 'data' to process. Must be non-negative and not exceed data.length.
     */
    public void updateData(byte[] data, int len) {
        if (!initialized)
            throw new IllegalStateException("ARC4 is not initialized. Call init() first.");
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (len < 0 || len > data.length)
            throw new IllegalArgumentException("Invalid len: " + len);

        try {
            // Update the same buffer as in C++ (input and output share the same array)
            int out = cipher.update(data, 0, len, data, 0);
            if (out != len) {
                // For a stream cipher, we normally expect out == len.
                // If it differs, fail fast to keep behavior predictable.
                throw new IllegalStateException("Unexpected output length: " + out + " (expected " + len + ")");
            }

            // In OpenSSL, the code calls EncryptFinal_ex (which adds nothing for RC4).
            // In Java, doFinal() may return 0 bytes, but can be called to "finalize".
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null && finalBytes.length != 0) {
                // For RC4, we do not expect bytes here; if we get any, it's unexpected.
                throw new IllegalStateException("doFinal() returned unexpected bytes for ARC4: " + finalBytes.length);
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to process ARC4", e);
        }
    }

    /**
     * "Replaces" the C++ destructor to release references/state.
     * (There is no explicit free like EVP_CIPHER_CTX_free, but this helps prevent reuse.)
     */
    @Override
    public void close() {
        // There is no direct "free" like EVP_CIPHER_CTX_free, but we can invalidate references.
        cipher = null;
        initialized = false;
    }
}
