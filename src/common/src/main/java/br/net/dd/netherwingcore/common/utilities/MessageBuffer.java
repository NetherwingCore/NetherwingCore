package br.net.dd.netherwingcore.common.utilities;

import java.util.Arrays;

public class MessageBuffer {

    private int writePosition;
    private int readPosition;
    private byte[] storage;

    // Default constructor, initializes with a default size of 4096 bytes.
    public MessageBuffer() {
        this(4096);
    }

    // Builder who accepts a starting size.
    public MessageBuffer(int initialSize) {
        this.writePosition = 0;
        this.readPosition = 0;
        this.storage = new byte[initialSize];
    }

    // Copy Builder
    public MessageBuffer(MessageBuffer other) {
        this.writePosition = other.writePosition;
        this.readPosition = other.readPosition;
        this.storage = Arrays.copyOf(other.storage, other.storage.length);
    }

    // Movement constructor (not required in Java, but we approximate this concept by reusing arrays)
    public MessageBuffer(MessageBuffer from, boolean move) {
        this.writePosition = from.writePosition;
        this.readPosition = from.readPosition;
        this.storage = from.storage;
        if (move) {
            from.reset();
        }
    }

    // Reset the buffer.
    public void reset() {
        this.writePosition = 0;
        this.readPosition = 0;
    }

    // Resize the buffer.
    public void resize(int newSize) {
        if (newSize != storage.length) {
            storage = Arrays.copyOf(storage, newSize);
        }
    }

    public byte[] getBasePointer() {
        return storage;
    }

    public byte[] getReadPointer() {
        return Arrays.copyOfRange(storage, readPosition, storage.length);
    }

    public byte[] getWritePointer() {
        return Arrays.copyOfRange(storage, writePosition, storage.length);
    }

    public void readCompleted(int bytes) {
        readPosition += bytes;
    }

    public void writeCompleted(int bytes) {
        writePosition += bytes;
    }

    public int getActiveSize() {
        return writePosition - readPosition;
    }

    public int getRemainingSpace() {
        return storage.length - writePosition;
    }

    public int getBufferSize() {
        return storage.length;
    }

    public void normalize() {
        if (readPosition > 0) {
            int activeSize = getActiveSize();
            if (readPosition != writePosition) {
                System.arraycopy(storage, readPosition, storage, 0, activeSize);
            }
            writePosition -= readPosition;
            readPosition = 0;
        }
    }

    public void ensureFreeSpace() {
        if (getRemainingSpace() == 0) {
            resize(storage.length * 3 / 2);
        }
    }

    public void write(byte[] data) {
        write(data, data.length);
    }

    public void write(byte[] data, int size) {
        ensureFreeSpace();
        System.arraycopy(data, 0, storage, writePosition, size);
        writeCompleted(size);
    }

    public byte[] move() {
        byte[] oldStorage = storage;
        reset();
        return oldStorage;
    }

    // Assignment operator
    public MessageBuffer assign(MessageBuffer other) {
        if (this != other) {
            this.writePosition = other.writePosition;
            this.readPosition = other.readPosition;
            this.storage = Arrays.copyOf(other.storage, other.storage.length);
        }
        return this;
    }

}
