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

public class AES {

    public static final int IV_SIZE_BYTES = 12;
    public static final int KEY_SIZE_BYTES = 16;
    public static final int TAG_SIZE_BYTES = 12;

    public record Key(byte[] data) {
        public Key(byte[] data) {
            if (data.length != KEY_SIZE_BYTES) {
                throw new IllegalArgumentException("Key must be " + KEY_SIZE_BYTES + " bytes long");
            }
            this.data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    public record IV(byte[] data) {
        public IV(byte[] data) {
            if (data.length != IV_SIZE_BYTES) {
                throw new IllegalArgumentException("IV must be " + IV_SIZE_BYTES + " bytes long");
            }
            this.data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    public record Tag(byte[] data) {
        public Tag(byte[] data) {
            if (data.length != TAG_SIZE_BYTES) {
                throw new IllegalArgumentException("IV must be " + TAG_SIZE_BYTES + " bytes long");
            }
            this.data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    private final boolean encrypting;
    private Cipher cipher;
    private SecretKey secretKey;

    public AES(boolean encrypting, int keySizeBits) {
        this.encrypting = encrypting;
        this.initCtx(keySizeBits);
    }

    public AES(boolean encrypting) {
        this.encrypting = encrypting;
        int keySizeBits = 128; // AES-128
        this.initCtx(keySizeBits);
    }

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

    public void init(byte[] key) {
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException("Key must be " + KEY_SIZE_BYTES + " bytes");
        }
        this.init(new Key(key));
    }

    public void init(Key key) {
        this.secretKey = new SecretKeySpec(key.data(), "AES");
    }

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



