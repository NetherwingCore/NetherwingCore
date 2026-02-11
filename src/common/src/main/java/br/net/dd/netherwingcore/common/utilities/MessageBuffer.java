package br.net.dd.netherwingcore.common.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A utility class for managing a byte buffer for reading and writing messages.
 * It supports dynamic resizing, reading/writing data, and handling little-endian byte order.
 */
public class MessageBuffer {

    private ByteBuffer buffer;
    private int readPosition;
    private int writePosition;

    /**
     * Constructs a MessageBuffer with the specified initial capacity.
     *
     * @param initialCapacity The initial capacity of the buffer in bytes.
     */
    public MessageBuffer(int initialCapacity) {
        this.buffer = ByteBuffer.allocate(initialCapacity);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.readPosition = 0;
        this.writePosition = 0;
    }

    /**
     * Constructs a MessageBuffer with a default initial capacity of 4096 bytes.
     */
    public MessageBuffer() {
        this(4096);
    }

    /**
     * Resizes the buffer to the specified new size. If the new size is smaller than the current capacity,
     * it simply adjusts the limit. If it's larger, it creates a new buffer and copies existing data.
     *
     * @param newSize The new size of the buffer in bytes.
     */
    public void resize(int newSize) {
        if (newSize <= buffer.capacity()) {
            buffer.limit(newSize);
            return;
        }

        ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
        newBuffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.flip();
        newBuffer.put(buffer);

        this.buffer = newBuffer;
    }

    /**
     * Writes the specified byte array to the buffer at the current write position.
     *
     * @param data   The byte array to write.
     * @param length The number of bytes from the array to write.
     */
    public void write(byte[] data, int length) {
        ensureCapacity(writePosition + length);
        buffer.position(writePosition);
        buffer.put(data, 0, length);
        writePosition += length;
    }

    /**
     * Writes the entire byte array to the buffer at the current write position.
     *
     * @param data The byte array to write.
     */
    public void write(byte[] data) {
        write(data, data.length);
    }

    /**
     * Reads a specified number of bytes from the buffer starting at the current read position.
     *
     * @param length The number of bytes to read.
     * @return A byte array containing the read data.
     * @throws IllegalStateException If there is not enough data to read.
     */
    public byte[] read(int length) {
        if (length > getActiveSize()) {
            throw new IllegalStateException("Not enough data to read");
        }

        byte[] result = new byte[length];
        buffer.position(readPosition);
        buffer.get(result);
        readPosition += length;

        return result;
    }

    /**
     * Gets a byte array containing the data at the current read position without advancing the read position.
     *
     * @param length The number of bytes to get.
     * @return A byte array containing the data at the current read position.
     * @throws IllegalStateException If there is not enough data to get.
     */
    public byte[] getReadPointer(int length) {
        byte[] result = new byte[length];
        int currentPos = buffer.position();

        buffer.position(readPosition);
        buffer.get(result);
        buffer.position(currentPos);

        return result;
    }

    /**
     * Advances the read position by the specified number of bytes after a successful read operation.
     *
     * @param bytesRead The number of bytes that were read.
     */
    public void readCompleted(int bytesRead) {
        readPosition += bytesRead;
    }

    /**
     * Advances the write position by the specified number of bytes after a successful write operation.
     *
     * @param bytesWritten The number of bytes that were written.
     */
    public void writeCompleted(int bytesWritten) {
        writePosition += bytesWritten;
    }

    /**
     * Gets the number of bytes currently available for reading in the buffer.
     *
     * @return The number of bytes available for reading.
     */
    public int getActiveSize() {
        return writePosition - readPosition;
    }

    /**
     * Gets the remaining capacity of the buffer for writing new data.
     *
     * @return The number of bytes available for writing.
     */
    public int getRemainingSpace() {
        return buffer.capacity() - writePosition;
    }

    /**
     * Gets the current read position in the buffer.
     *
     * @return The current read position.
     */
    public int getWritePosition() {
        return writePosition;
    }

    /**
     * Resets the buffer by clearing its contents and resetting the read and write positions to zero.
     */
    public void reset() {
        readPosition = 0;
        writePosition = 0;
        buffer.clear();
    }

    /**
     * Converts the active portion of the buffer (from the current read position to the current write position) into a byte array.
     *
     * @return A byte array containing the active data in the buffer.
     */
    public byte[] toArray() {
        byte[] result = new byte[getActiveSize()];
        buffer.position(readPosition);
        buffer.get(result);
        return result;
    }

    /**
     * Ensures that the buffer has enough capacity to accommodate the required number of bytes. If not, it resizes the buffer.
     *
     * @param required The total number of bytes required in the buffer.
     */
    private void ensureCapacity(int required) {
        if (required > buffer.capacity()) {
            int newCapacity = Math.max(required, buffer.capacity() * 2);
            resize(newCapacity);
        }
    }

    /**
     * Reads an unsigned short (2 bytes) from the buffer in little-endian order and returns it as an int.
     *
     * @return The unsigned short value read from the buffer (0 to 65535).
     */
    public int readUnsignedShortLE() {
        byte[] bytes = read(2);
        return ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
    }

    /**
     * Writes an unsigned short (2 bytes) to the buffer in little-endian order.
     *
     * @param value The unsigned short value to write (0 to 65535).
     */
    public void writeShortLE(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        write(bytes);
    }

}
