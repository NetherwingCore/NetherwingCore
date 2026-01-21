package br.net.dd.netherwingcore.common.cryptography.authentication;

import br.net.dd.netherwingcore.common.cryptography.AES;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class WorldPacketCrypt {

    public record Key(byte[] data) {
        public Key(byte[] data) {
            if (data.length != 32) {
                throw new IllegalArgumentException("Key must be " + 32 + " bytes long");
            }
            this.data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    public static class WorldPacketCryptIV {
        public static final int SIZE = 12;
        private final byte[] value = new byte[SIZE];

        public WorldPacketCryptIV(long counter, int magic) {
            // We use ByteBuffer to write the values in little-endian or big-endian.
            // as expected. Here I will use little-endian as an example.
            ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(counter); // 8 bytes
            buffer.putInt(magic); // 4 bytes
        }

        public byte[] getValue() {
            return Arrays.copyOf(value, SIZE);
        }
    }

    private final AES clientDecrypt;
    private final AES serverEncrypt;

    private Integer clientCounter;
    private Integer serverCounter;
    private boolean initialized;

    public WorldPacketCrypt() throws Exception {
        clientDecrypt = new AES(false, 256);
        serverEncrypt = new AES(true, 256);
        clientCounter = 0;
        serverCounter = 0;
        initialized = false;
    }

    public void init(Key key) {
        this.clientDecrypt.init(key.data);
        this.serverEncrypt.init(key.data);
        initialized = true;
    }

    public boolean peekDecryptRecv(byte[] data, int length) {
        if (initialized) {
            WorldPacketCryptIV iv = new WorldPacketCryptIV(clientCounter, 0x544E4C43);
            if (!clientDecrypt.processNoIntegrityCheck(new AES.IV(iv.value), data, length)) {
                return false;
            }
        }

        return true;
    }

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

    public boolean isInitialized() {
        return initialized;
    }

}
