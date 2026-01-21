package br.net.dd.netherwingcore.common.cryptography.authentication;

import br.net.dd.netherwingcore.common.cryptography.AES;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * The {@code WorldPacketCrypt} class is responsible for encrypting and decrypting
 * network packets with an implementation based on AES encryption. This class manages
 * both packet decryption for incoming data and encryption for outgoing data.
 *
 * It also handles initialization vectors (IVs) and counters for the encryption
 * and decryption processes to ensure data integrity and security.
 */
public class WorldPacketCrypt {

    /**
     * Represents a cryptographic key used for initialization of the encryption
     * and decryption algorithms. The key must be 32 bytes in length.
     */
    public record Key(byte[] data) {

        /**
         * Constructs a new {@code Key} instance.
         *
         * @param data A byte array representing the key data. Must be exactly 32 bytes long.
         * @throws IllegalArgumentException If the provided key is not 32 bytes long.
         */
        public Key(byte[] data) {
            if (data.length != 32) {
                throw new IllegalArgumentException("Key must be " + 32 + " bytes long");
            }
            this.data = data.clone();
        }

        /**
         * Gets the key data as a cloned byte array, ensuring immutability.
         *
         * @return A cloned byte array of the key data.
         */
        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    /**
     * Represents the Initialization Vector (IV) used in encryption and decryption processes.
     * This vector is based on a counter and a magic constant and is used for creating
     * secure packet encryption and decryption.
     */
    public static class WorldPacketCryptIV {
        /** The size of the IV in bytes. */
        public static final int SIZE = 12;
        private final byte[] value = new byte[SIZE];

        /**
         * Constructs an IV using the given counter and magic value.
         *
         * @param counter A long value used as part of the IV.
         * @param magic   An integer magic constant to differentiate IVs.
         */
        public WorldPacketCryptIV(long counter, int magic) {
            // Initialize the IV using little-endian order.
            ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(counter); // 8 bytes
            buffer.putInt(magic); // 4 bytes
        }

        /**
         * Gets the IV value as a cloned byte array.
         *
         * @return A cloned byte array of the IV value.
         */
        public byte[] getValue() {
            return Arrays.copyOf(value, SIZE);
        }
    }

    // AES encryptor and decryptor instances for client and server
    private final AES clientDecrypt;
    private final AES serverEncrypt;

    // Counters for client and server encryption/decryption
    private Integer clientCounter;
    private Integer serverCounter;

    // Tracks whether the cryptographic system is initialized
    private boolean initialized;

    /**
     * Constructs a new {@code WorldPacketCrypt} instance with default AES configurations.
     *
     * @throws Exception If the AES initialization fails.
     */
    public WorldPacketCrypt() throws Exception {
        clientDecrypt = new AES(false, 256);
        serverEncrypt = new AES(true, 256);
        clientCounter = 0;
        serverCounter = 0;
        initialized = false;
    }

    /**
     * Initializes the cryptographic system with the given cryptographic key.
     *
     * @param key The cryptographic key used for both encryption and decryption.
     */
    public void init(Key key) {
        this.clientDecrypt.init(key.data);
        this.serverEncrypt.init(key.data);
        initialized = true;
    }

    /**
     * Performs a non-destructive decryption on the given data to verify its integrity,
     * without modifying the input data's integrity.
     *
     * @param data   The data to be decrypted.
     * @param length The length of the data to be decrypted.
     * @return {@code true} if the data can be successfully decrypted; {@code false} otherwise.
     */
    public boolean peekDecryptRecv(byte[] data, int length) {
        if (initialized) {
            WorldPacketCryptIV iv = new WorldPacketCryptIV(clientCounter, 0x544E4C43);
            if (!clientDecrypt.processNoIntegrityCheck(new AES.IV(iv.value), data, length)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Decrypts the received data using the current client counter and verification tag.
     *
     * @param data   The data to be decrypted.
     * @param length The length of the data to be decrypted.
     * @param tag    The cryptographic tag used for integrity verification.
     * @return {@code true} if decryption is successful; {@code false} otherwise.
     */
    public boolean decryptRecv(byte[] data, int length, AES.Tag tag) {
        if  (initialized) {
            WorldPacketCryptIV iv = new WorldPacketCryptIV(clientCounter, 0x544E4C43);
            if (!clientDecrypt.process(new AES.IV(iv.value), data, length, tag)) {
                return false;
            }
        } else {
            tag.clear();
        }

        clientCounter++;
        return true;
    }

    /**
     * Encrypts the given data using the current server counter and verification tag.
     *
     * @param data   The data to be encrypted.
     * @param length The length of the data to be encrypted.
     * @param tag    The cryptographic tag used for integrity verification.
     * @return {@code true} if encryption is successful; {@code false} otherwise.
     */
    public boolean encryptSend(byte[] data, int length, AES.Tag tag) {
        if (initialized) {
            WorldPacketCryptIV iv = new WorldPacketCryptIV(serverCounter, 0x52565253);
            if (!serverEncrypt.process(new AES.IV(iv.value), data, length, tag)) {
                return false;
            }
        } else {
            tag.clear();
        }

        serverCounter++;
        return true;
    }

    /**
     * Checks whether the cryptographic system has been initialized.
     *
     * @return {@code true} if the cryptographic system is initialized; {@code false} otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

}
