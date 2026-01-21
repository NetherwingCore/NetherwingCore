package br.net.dd.netherwingcore.common.cryptography;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Utility class for AES (Advanced Encryption Standard) based cryptography operations.
 * This class supports both encryption and decryption of data using AES GCM (Galois/Counter Mode).
 * It provides functionality for key, IV (Initialization Vector), and tag management.
 *
 * <p>
 * Features:
 * - Supports AES-128 encryption mode.
 * - Provides wrappers for key, IV, and tag with automatic validation.
 * - Handles encryption and decryption integrity checks.
 * - Includes optional processing without integrity verification (uses AES/CTR mode).
 * </p>
 */
public class AES {

    public static final int IV_SIZE_BYTES = 12;
    public static final int KEY_SIZE_BYTES = 16;
    public static final int TAG_SIZE_BYTES = 12;

    /**
     * Represents an AES key encapsulating its byte data and validation logic.
     * Ensures the key length conforms to {@link AES#KEY_SIZE_BYTES}.
     */
    public record Key(byte[] data) {
        public Key(byte[] data) {
            if (data.length != KEY_SIZE_BYTES) {
                throw new IllegalArgumentException("Key must be " + KEY_SIZE_BYTES + " bytes long");
            }
            this.data = data.clone();
        }

        /**
         * Clears the key data by overwriting its byte array with zeroes.
         */
        public void clear() {
            Arrays.fill(data, (byte) 0);
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    /**
     * Represents an AES Initialization Vector (IV).
     * Validates the size of the IV matches {@link AES#IV_SIZE_BYTES}.
     */
    public record IV(byte[] data) {
        public IV(byte[] data) {
            if (data.length != IV_SIZE_BYTES) {
                throw new IllegalArgumentException("IV must be " + IV_SIZE_BYTES + " bytes long");
            }
            this.data = data.clone();
        }

        /**
         * Clears the IV data by overwriting its byte array with zeroes.
         */
        public void clear() {
            Arrays.fill(data, (byte) 0);
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    /**
     * Represents an AES authentication tag.
     * Validates the size of the tag matches {@link AES#TAG_SIZE_BYTES}.
     */
    public record Tag(byte[] data) {
        public Tag(byte[] data) {
            if (data.length != TAG_SIZE_BYTES) {
                throw new IllegalArgumentException("IV must be " + TAG_SIZE_BYTES + " bytes long");
            }
            this.data = data.clone();
        }

        /**
         * Clears the tag data by overwriting its byte array with zeroes.
         */
        public void clear() {
            Arrays.fill(data, (byte) 0);
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    private final boolean encrypting;
    private Cipher cipher;
    private SecretKey secretKey;

    /**
     * Constructs an AES instance for encryption or decryption.
     *
     * @param encrypting true if this instance is for encryption, false for decryption.
     * @param keySizeBits the size of the AES key in bits (must be 128, 192, or 256).
     */
    public AES(boolean encrypting, int keySizeBits) {
        this.encrypting = encrypting;
        this.initCtx(keySizeBits);
    }

    /**
     * Constructs an AES instance for encryption or decryption with a default key size of 128 bits.
     *
     * @param encrypting true if this instance is for encryption, false for decryption.
     */
    public AES(boolean encrypting) {
        this.encrypting = encrypting;
        int keySizeBits = 128; // AES-128
        this.initCtx(keySizeBits);
    }

    /**
     * Initializes the cipher context with the specified key size.
     *
     * @param keySizeBits the size of the AES key in bits (valid values: 128, 192, 256).
     * @throws IllegalArgumentException if an invalid key size is provided.
     */
    private void initCtx(int keySizeBits) {
        if (keySizeBits != 128 && keySizeBits != 192 && keySizeBits != 256) {
            throw new IllegalArgumentException("Invalid AES key size: " + keySizeBits);
        }

        try {
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the AES instance with the specified key.
     *
     * @param key the AES key as a byte array.
     * @throws IllegalArgumentException if the key length is not {@link AES#KEY_SIZE_BYTES}.
     */
    public void init(byte[] key) {
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException("Key must be " + KEY_SIZE_BYTES + " bytes");
        }
        this.init(new Key(key));
    }

    /**
     * Initializes the AES instance with the specified {@link Key}.
     *
     * @param key the AES key encapsulated in a {@link Key} object.
     */
    public void init(Key key) {
        this.secretKey = new SecretKeySpec(key.data(), "AES");
    }

    /**
     * Processes encryption or decryption for AES using the specified IV and Tag.
     *
     * @param iv the initialization vector used for AES GCM mode.
     * @param data the input data for encryption or decryption.
     * @param length the length of the data to process.
     * @param tag the output tag (for encryption) or input tag (for decryption).
     * @return true if the operation succeeds, false otherwise.
     */
    public boolean process(IV iv, byte[] data, int length, Tag tag) {
        try {
            if (iv.data.length != IV_SIZE_BYTES) {
                log("AES process error: The IV should be size "+ IV_SIZE_BYTES +", but it's size "+ iv.data.length +".");
                return false;
            }
            if (tag.data.length != TAG_SIZE_BYTES) {
                log("AES process error: The TAG should be size "+ TAG_SIZE_BYTES +", but it's size "+ tag.data.length +".");
                return false;
            }

            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE_BYTES * 8, iv.data);
            if (this.encrypting) {
                this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, gcmSpec);
            } else {
                this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey, gcmSpec);
            }

            byte[] output = this.cipher.doFinal(Arrays.copyOf(data, length));

            if(this.encrypting) {
                // Extract the tag from the last TAG_SIZE_BYTES
                System.arraycopy(output, output.length - TAG_SIZE_BYTES, tag.data, 0, TAG_SIZE_BYTES);
                // Copy the ciphertext back to data
                byte[] ciphertext = Arrays.copyOf(output, output.length - TAG_SIZE_BYTES);
                System.arraycopy(ciphertext, 0, data, 0, ciphertext.length);
            } else {
                // In the decryption, 'data' must contain ciphertext+tag
                // The result is plaintext
                System.arraycopy(output, 0, data, 0, output.length);
            }

            return true;

        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            log("AES process error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Processes decryption without integrity checks using AES CTR (Counter Mode).
     *
     * <p>Note: This mode does not provide data authenticity or integrity validation.</p>
     *
     * @param iv the initialization vector for AES CTR mode.
     * @param data the input ciphertext for decryption.
     * @param partialLength the length of the input data.
     * @return true if the operation is successful, false otherwise.
     */
    public boolean processNoIntegrityCheck(IV iv, byte[] data, int partialLength) {
        try {
            if (encrypting) {
                log("AES processNoIntegrityCheck error: Integrity check is required when encrypting.");
                return false;
            }
            if (iv.data.length != IV_SIZE_BYTES) {
                log("AES processNoIntegrityCheck error: The IV should be size "+ IV_SIZE_BYTES +", but it's size "+ iv.data.length +".");
                return false;
            }
            if (partialLength > data.length) {
                log("AES processNoIntegrityCheck error: partialLength exceeds data length.");
                return false;
            }

            // We use AES/CTR/NoPadding to simulate "no integrity"
            this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.data);
            this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey, ivSpec);

            byte[] input = Arrays.copyOf(data, partialLength);
            byte[] result = this.cipher.doFinal(input);

            // Copy the result back to the original buffer.
            System.arraycopy(result, 0, data, 0, result.length);

            return true;

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            log("AES processNoIntegrityCheck error: " + e.getMessage());
            return  false;
        }
    }

}



