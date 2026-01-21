package br.net.dd.netherwingcore.common.utilities;

import java.util.Arrays;

/**
 * A utility class for managing a buffer of bytes, with functionality for
 * reading, writing, resizing, and resetting the buffer.
 * This class is useful for handling byte streams and provides methods for
 * efficient memory management.
 */
public class MessageBuffer {

    private int writePosition;
    private int readPosition;
    private byte[] storage;

    /**
     * Default constructor, initializes the buffer with a default size of 4096 bytes.
     */
    public MessageBuffer() {
        this(4096);
    }

    /**
     * Constructor that initializes the buffer with a specified initial size.
     *
     * @param initialSize the initial size of the buffer.
     */
    public MessageBuffer(int initialSize) {
        this.writePosition = 0;
        this.readPosition = 0;
        this.storage = new byte[initialSize];
    }

    /**
     * Copy constructor. Creates a new buffer that is a copy of the given buffer.
     *
     * @param other the buffer to copy from.
     */
    public MessageBuffer(MessageBuffer other) {
        this.writePosition = other.writePosition;
        this.readPosition = other.readPosition;
        this.storage = Arrays.copyOf(other.storage, other.storage.length);
    }

    /**
     * Movement constructor. Creates a new buffer by reusing the storage
     * of the given buffer. Optionally resets the original buffer.
     *
     * @param from the buffer to move from.
     * @param move if true, the original buffer is reset.
     */
    public MessageBuffer(MessageBuffer from, boolean move) {
        this.writePosition = from.writePosition;
        this.readPosition = from.readPosition;
        this.storage = from.storage;
        if (move) {
            from.reset();
        }
    }

    /**
     * Resets the buffer. Clears the read and write positions, effectively
     * making the buffer empty.
     */
    public void reset() {
        this.writePosition = 0;
        this.readPosition = 0;
    }

    /**
     * Resizes the buffer to the specified new size.
     *
     * @param newSize the new size of the buffer.
     */
    public void resize(int newSize) {
        if (newSize != storage.length) {
            storage = Arrays.copyOf(storage, newSize);
        }
    }

    /**
     * Retrieves the underlying byte array of the buffer.
     *
     * @return the storage array of the buffer.
     */
    public byte[] getBasePointer() {
        return storage;
    }

    /**
     * Retrieves the portion of the buffer that is available for reading.
     *
     * @return a copy of the read portion of the buffer.
     */
    public byte[] getReadPointer() {
        return Arrays.copyOfRange(storage, readPosition, storage.length);
    }

    /**
     * Retrieves the portion of the buffer that is available for writing.
     *
     * @return a copy of the writable portion of the buffer.
     */
    public byte[] getWritePointer() {
        return Arrays.copyOfRange(storage, writePosition, storage.length);
    }

    /**
     * Marks a specified number of bytes as read from the buffer.
     *
     * @param bytes the number of bytes to mark as read.
     */
    public void readCompleted(int bytes) {
        readPosition += bytes;
    }

    /**
     * Marks a specified number of bytes as written to the buffer.
     *
     * @param bytes the number of bytes to mark as written.
     */
    public void writeCompleted(int bytes) {
        writePosition += bytes;
    }

    /**
     * Calculates the size of the active data in the buffer (data between
     * the read and write positions).
     *
     * @return the active size of the buffer.
     */
    public int getActiveSize() {
        return writePosition - readPosition;
    }

    /**
     * Calculates the remaining free space in the buffer (space between
     * the write position and the end of the buffer).
     *
     * @return the remaining free space.
     */
    public int getRemainingSpace() {
        return storage.length - writePosition;
    }

    /**
     * Retrieves the total capacity of the buffer.
     *
     * @return the total size of the buffer.
     */
    public int getBufferSize() {
        return storage.length;
    }

    /**
     * Normalizes the buffer by moving unread data to the beginning,
     * freeing up space for new writes. This minimizes fragmentation.
     */
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

    /**
     * Ensures that there is free space available in the buffer. If no free
     * space remains, the buffer is resized to 1.5 times its current size.
     */
    public void ensureFreeSpace() {
        if (getRemainingSpace() == 0) {
            resize(storage.length * 3 / 2);
        }
    }

    /**
     * Writes a byte array to the buffer.
     *
     * @param data the data to write.
     */
    public void write(byte[] data) {
        write(data, data.length);
    }

    /**
     * Writes a portion of a byte array to the buffer.
     *
     * @param data the data to write.
     * @param size the number of bytes to write.
     */
    public void write(byte[] data, int size) {
        ensureFreeSpace();
        System.arraycopy(data, 0, storage, writePosition, size);
        writeCompleted(size);
    }

    /**
     * Moves the storage to a new byte array and resets the buffer.
     *
     * @return the old storage array.
     */
    public byte[] move() {
        byte[] oldStorage = storage;
        reset();
        return oldStorage;
    }

    /**
     * Assignment method. Copies the content of another buffer into this one.
     *
     * @param other the buffer to copy from.
     * @return this buffer, updated with the content of the other buffer.
     */
    public MessageBuffer assign(MessageBuffer other) {
        if (this != other) {
            this.writePosition = other.writePosition;
            this.readPosition = other.readPosition;
            this.storage = Arrays.copyOf(other.storage, other.storage.length);
        }
        return this;
    }

}
