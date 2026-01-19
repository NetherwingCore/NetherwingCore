package br.net.dd.netherwingcore.shared.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Socket {
    private final AsynchronousSocketChannel socketChannel;
    private final InetSocketAddress remoteAddress;
    private final ByteBuffer readBuffer;
    private final ConcurrentLinkedQueue<ByteBuffer> writeQueue;
    private final AtomicBoolean closed;
    private final AtomicBoolean closing;
    private final AtomicBoolean isWritingAsync;

    public Socket(AsynchronousSocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        this.remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
        this.readBuffer = ByteBuffer.allocate(4096); // READ_BLOCK_SIZE
        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.closed = new AtomicBoolean(false);
        this.closing = new AtomicBoolean(false);
        this.isWritingAsync = new AtomicBoolean(false);
    }

    public abstract void start();

    public abstract void readHandler();

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public boolean isOpen() {
        return !closed.get() && !closing.get();
    }

    public void asyncRead() {
        if (!isOpen()) return;

        readBuffer.clear();
        socketChannel.read(readBuffer, this, new CompletionHandler<Integer, Socket>() {
            @Override
            public void completed(Integer bytesRead, Socket socket) {
                if (bytesRead == -1) {
                    closeSocket(); // End-of-stream reached
                    return;
                }
                readBuffer.flip();
                readHandler(); // Delegate to the subclass's handler
            }

            @Override
            public void failed(Throwable exc, Socket socket) {
                closeSocket();
            }
        });
    }

    public void queuePacket(ByteBuffer buffer) {
        writeQueue.offer(buffer);

        // Process the queue asynchronously
        asyncProcessQueue();
    }

    private void asyncProcessQueue() {
        if (isWritingAsync.compareAndSet(false, true)) {
            ByteBuffer buffer = writeQueue.peek();
            if (buffer == null) {
                isWritingAsync.set(false);
                return;
            }

            socketChannel.write(buffer, this, new CompletionHandler<Integer, Socket>() {
                @Override
                public void completed(Integer bytesWritten, Socket socket) {
                    buffer.position(buffer.position() + bytesWritten);
                    if (buffer.remaining() == 0) {
                        writeQueue.poll(); // Remove the buffer if fully written
                    }

                    // Continue processing the queue or finalize closure
                    isWritingAsync.set(false);
                    if (closing.get() && writeQueue.isEmpty()) {
                        closeSocket();
                    } else {
                        asyncProcessQueue();
                    }
                }

                @Override
                public void failed(Throwable exc, Socket socket) {
                    closeSocket();
                }
            });
        }
    }

    public void closeSocket() {
        if (!closed.compareAndSet(false, true)) return;

        try {
            socketChannel.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace(); // Log or handle the error
        }

        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace(); // Log or handle the error
        }

        onClose();
    }

    public void delayedCloseSocket() {
        closing.set(true);
    }

    public void setNoDelay(boolean enable) {
        try {
            socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, enable);
        } catch (IOException e) {
            e.printStackTrace(); // Log or handle the error
        }
    }

    protected void onClose() {
        // Hook for any subclass-specific close logic
    }
}
