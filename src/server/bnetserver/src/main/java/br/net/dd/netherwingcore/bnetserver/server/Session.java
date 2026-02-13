package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.bnetserver.services.ServiceDispatcher;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto.Header;
import br.net.dd.netherwingcore.shared.networking.SocketReadCallbackResult;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a client session in the BNet server. This class manages the state of a single client connection,
 * including reading and writing data, handling SSL encryption/decryption, and dispatching requests to the appropriate services.
 * Each session is associated with a SocketChannel and an SSLEngine for secure communication.
 */
public class Session {

    private static final Log logger = Log.getLogger(Session.class.getSimpleName());

    private final SocketChannel socketChannel;
    private final SSLEngine sslEngine;
    private final ServiceDispatcher serviceDispatcher;

    // Buffers for reading the incoming message in stages.
    private final MessageBuffer headerLengthBuffer;
    private final MessageBuffer headerBuffer;
    private final MessageBuffer packetBuffer;

    // Buffer for accumulating data read from the socket.
    private final MessageBuffer readBuffer;

    // SSL Buffers - IMPORTANT: Manage position correctly
    private final ByteBuffer peerNetData;   // Encrypted data received from the network
    private ByteBuffer peerAppData;         // Decrypted data from the application
    private ByteBuffer myNetData;           // Encrypted data to send
    private final ByteBuffer myAppData;     // Application data to encrypt

    // Queue for outgoing messages to be written to the socket.
    private final ConcurrentLinkedQueue<MessageBuffer> writeQueue;

    // Authentication state and account information.
    private volatile boolean authenticated;
    private volatile boolean initialHandshakeDone;
    private String accountName;
    private int accountId;

    // Timing for idle connection management and handshake timeouts.
    private volatile long lastActivityTime;
    private volatile long handshakeCompletedTime;
    private static final long IDLE_TIMEOUT_MS = 30000; // 30 seconds
    private static final long POST_HANDSHAKE_TIMEOUT_MS = 5000; // 5 seconds after handshake completion

    private volatile boolean closed = false;

    /**
     * Constructs a new Session for a given SocketChannel and SSLEngine.
     *
     * @param socketChannel The SocketChannel associated with this session.
     * @param sslEngine     The SSLEngine for handling SSL encryption/decryption for this session.
     */
    public Session(SocketChannel socketChannel, SSLEngine sslEngine) {
        this.socketChannel = socketChannel;
        this.sslEngine = sslEngine;
        this.serviceDispatcher = ServiceDispatcher.getInstance();

        this.headerLengthBuffer = new MessageBuffer(2);
        this.headerBuffer = new MessageBuffer();
        this.packetBuffer = new MessageBuffer();
        this.readBuffer = new MessageBuffer(8192);

        // Initializes SSL buffers based on the engine's session requirements.
        int netBufferSize = sslEngine.getSession().getPacketBufferSize();
        int appBufferSize = sslEngine.getSession().getApplicationBufferSize();

        // These buffers will be used for SSL handshakes and encryption/decryption.
        this.peerNetData = ByteBuffer.allocate(netBufferSize);
        this.peerAppData = ByteBuffer.allocate(appBufferSize);
        this.myNetData = ByteBuffer.allocate(netBufferSize);
        this.myAppData = ByteBuffer.allocate(appBufferSize);

        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.authenticated = false;
        this.initialHandshakeDone = false;

        this.lastActivityTime = System.currentTimeMillis();
        this.handshakeCompletedTime = -1; // Indicates handshake not completed yet

        logger.debug("New session created for {}", getClientInfo());
    }

    /**
     * Starts the SSL handshake process for this session. This method will initiate the handshake and handle the
     * necessary steps to establish a secure connection with the client. If the handshake fails, the session will be closed.
     */
    public void start() {

        logger.debug("{} Starting SSL handshake", getClientInfo());

        try {
            // Starts the SSL handshake process.
            sslEngine.beginHandshake();

            logger.debug("{} Handshake status: {}",
                    getClientInfo(), String.valueOf(sslEngine.getHandshakeStatus()));

        } catch (IOException e) {
            logger.error("{} Failed to start SSL handshake: {}",
                    getClientInfo(), e.getMessage());
            closeSocket();
        }

    }

    /**
     * Checks if the session has been idle for longer than the specified timeout. This method compares the current time with the last recorded activity time
     * to determine if the session should be considered idle and potentially closed due to inactivity.
     *
     * @return true if the session has been idle for longer than the specified timeout, false otherwise.
     */
    public boolean isIdle() {
        long now = System.currentTimeMillis();

        // If the handshake is completed but no application data has been received within the post-handshake timeout, consider it idle
        if (initialHandshakeDone && handshakeCompletedTime > 0) {
            long timeSinceHandshake = now - handshakeCompletedTime;
            if (timeSinceHandshake > POST_HANDSHAKE_TIMEOUT_MS && readBuffer.getActiveSize() == 0) {
                logger.warn("{} No data received {}ms after handshake completion",
                        getClientInfo(), timeSinceHandshake);
                return true;
            }
        }

        // Otherwise, check if the session has been idle based on the last activity time
        return (now - lastActivityTime) > IDLE_TIMEOUT_MS;
    }

    /**
     * Reads data from the socket and processes it through the SSL engine. This method handles reading encrypted data from the network,
     * decrypting it using the SSLEngine, and storing the decrypted application data in the read buffer for further processing.
     * It also manages the SSL handshake state and handles any SSL exceptions that may occur during the unwrap process.
     *
     * @param tempBuffer A temporary ByteBuffer used for reading data from the socket.
     * @return The number of bytes read from the socket, or -1 if the connection was closed by the client.
     * @throws IOException If an I/O error occurs during reading from the socket or processing with the SSLEngine.
     */
    public int readFromSocket(ByteBuffer tempBuffer) throws IOException {
        // Read encrypted data from network
        int bytesRead = socketChannel.read(peerNetData);

        if (bytesRead > 0) {
            this.lastActivityTime = System.currentTimeMillis();

            logger.debug("{} Read {} bytes from network", getClientInfo(), bytesRead);
            peerNetData.flip();

            // Process the data
            while (peerNetData.hasRemaining()) {
                peerAppData.clear();

                SSLEngineResult result;
                try {
                    result = sslEngine.unwrap(peerNetData, peerAppData);
                } catch (SSLException e) {
                    logger.error("{} SSLException during unwrap: {}",
                            getClientInfo(), e.getMessage());
                    // Don't close immediately, let the connection recover
                    peerNetData.compact();
                    return bytesRead;
                }

                logger.debug("{} unwrap: status={}, hsStatus={}, consumed={}, produced={}",
                        getClientInfo(),
                        result.getStatus(),
                        result.getHandshakeStatus(),
                        result.bytesConsumed(),
                        result.bytesProduced());

                switch (result.getStatus()) {
                    case OK:
                        peerAppData.flip();

                        // Process handshake if needed
                        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                            runDelegatedTasks();
                        }

                        // Check if handshake completed
                        if (!initialHandshakeDone &&
                                (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED ||
                                        result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)) {
                            initialHandshakeDone = true;
                            handshakeCompletedTime = System.currentTimeMillis(); // Record handshake completion time

                            SSLSession sslSession = sslEngine.getSession();
                            logger.debug("╔══════════════════════════════════════════════════════");
                            logger.debug("║ {} SSL Handshake Completed", getClientInfo());
                            logger.debug("╠══════════════════════════════════════════════════════");
                            logger.debug("║ Protocol:      {}", sslSession.getProtocol());
                            logger.debug("║ Cipher Suite:  {}", sslSession.getCipherSuite());
                            logger.debug("║ Session ID:    {}",
                                    bytesToHex(sslSession.getId()).substring(0,
                                            Math.min(32, bytesToHex(sslSession.getId()).length())));
                            logger.debug("║ Valid:         {}", sslSession.isValid());

                            try {
                                var peerCerts = sslSession.getPeerCertificates();
                                logger.debug("║ Peer Certs:    {} certificate(s)", peerCerts.length);
                            } catch (Exception e) {
                                logger.debug("║ Peer Certs:    None (server doesn't require client cert)");
                            }

                            logger.debug("╚══════════════════════════════════════════════════════");
                            logger.debug("{} ⏳ Waiting for client to send application data...", getClientInfo());

                        }

                        // If we have application data, store it
                        if (peerAppData.hasRemaining()) {
                            byte[] data = new byte[peerAppData.remaining()];
                            peerAppData.get(data);
                            readBuffer.write(data);

                            logger.debug("╔══════════════════════════════════════════════════════");
                            logger.debug("║ {} Received Application Data", getClientInfo());
                            logger.debug("╠══════════════════════════════════════════════════════");
                            logger.debug("║ Size: {} bytes", data.length);
                            if (data.length > 0) {
                                logger.debug("║ First bytes (hex): {}",
                                        bytesToHex(Arrays.copyOf(data, Math.min(32, data.length))));
                            }
                            logger.debug("╚══════════════════════════════════════════════════════");

                        }
                        break;

                    case BUFFER_OVERFLOW:
                        // Need larger app buffer
                        peerAppData = enlargeApplicationBuffer(peerAppData);
                        break;

                    case BUFFER_UNDERFLOW:
                        // Need more network data - this is NORMAL
                        logger.debug("{} Buffer underflow, waiting for more data", getClientInfo());
                        peerNetData.compact();
                        return bytesRead;

                    case CLOSED:
                        logger.info("{} Peer closed SSL connection cleanly", getClientInfo());
                        closeSocket();
                        return -1;
                }
            }

            peerNetData.compact();

        } else if (bytesRead == 0) {
            // No data available - this is NORMAL for non-blocking I/O
            logger.debug("{} No data available to read", getClientInfo());
            return 0;

        } else { // bytesRead < 0
            // Only log and close if we're sure it's EOF
            if (initialHandshakeDone && handshakeCompletedTime > 0) {
                long timeSinceHandshake = System.currentTimeMillis() - handshakeCompletedTime;
                if (readBuffer.getActiveSize() == 0) {
                    logger.warn("╔══════════════════════════════════════════════════════");
                    logger.warn("║ {} Client Disconnected", getClientInfo());
                    logger.warn("╠══════════════════════════════════════════════════════");
                    logger.warn("║ Time since handshake: {}ms", timeSinceHandshake);
                    logger.warn("║ Data received: 0 bytes");
                    logger.warn("║ Reason: Client closed connection without sending data");
                    logger.warn("║ Likely causes:");
                    logger.warn("║   - Testing SSL connectivity only");
                    logger.warn("║   - Certificate not trusted");
                    logger.warn("║   - Not a real Battle.net client");
                    logger.warn("╚══════════════════════════════════════════════════════");
                } else {
                    logger.info("{} Client disconnected after sending {} bytes",
                            getClientInfo(), readBuffer.getActiveSize());
                }
            } else {
                logger.warn("{} Client disconnected before handshake completed", getClientInfo());
            }
            closeSocket();
            return -1;
        }

        return bytesRead;
    }

    /**
     * Helper method to convert byte array to hex string for logging purposes. This is used to log the session ID in a readable format.
     *
     * @param bytes The byte array to convert to hex string.
     * @return A string representation of the byte array in hexadecimal format.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    /**
     * Processes the write queue by encrypting and sending any pending application data to the client.
     * This method handles SSL wrapping of the application data and manages the SSL handshake state if necessary.
     * It will attempt to send all queued messages, handling buffer resizing as needed. If the handshake is not yet complete,
     * it will prioritize completing the handshake before sending application data.
     *
     * @return true if all data in the write queue was successfully sent, false if there is still data pending to be sent.
     * @throws IOException If an I/O error occurs during the SSL wrapping or socket writing process.
     */
    public boolean processWriteQueue() throws IOException {
        // Handle handshake wrapping if needed
        SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();

        if (hsStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
            logger.debug("{} Handshake needs WRAP", getClientInfo());

            myAppData.clear();
            myNetData.clear();

            SSLEngineResult result = sslEngine.wrap(myAppData, myNetData);

            logger.debug("{} wrap: status={}, hsStatus={}, produced={}",
                    getClientInfo(),
                    result.getStatus(),
                    result.getHandshakeStatus(),
                    result.bytesProduced());

            if (result.getStatus() == SSLEngineResult.Status.OK) {
                myNetData.flip();

                while (myNetData.hasRemaining()) {
                    socketChannel.write(myNetData);
                }

                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegatedTasks();
                }

                // Check if handshake completed
                if (!initialHandshakeDone &&
                        (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED ||
                                result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)) {
                    initialHandshakeDone = true;
                    logger.info("{} SSL handshake completed (after wrap)", getClientInfo());
                }
            }
        }

        // Don't send application data until handshake is done
        if (!initialHandshakeDone) {
            return hsStatus == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        }

        // Process application data queue
        while (!writeQueue.isEmpty()) {
            MessageBuffer packet = writeQueue.peek();

            myAppData.clear();
            myAppData.put(packet.toArray());
            myAppData.flip();

            myNetData.clear();

            SSLEngineResult result = sslEngine.wrap(myAppData, myNetData);

            switch (result.getStatus()) {
                case OK:
                    myNetData.flip();

                    while (myNetData.hasRemaining()) {
                        int written = socketChannel.write(myNetData);
                        if (written == 0) {
                            // Can't write more now
                            return false;
                        }
                    }

                    logger.debug("{} Sent encrypted packet: {} bytes",
                            getClientInfo(), result.bytesProduced());

                    writeQueue.poll(); // Successfully sent
                    break;

                case BUFFER_OVERFLOW:
                    myNetData = enlargePacketBuffer(myNetData);
                    break;

                case BUFFER_UNDERFLOW:
                    logger.fatal("{} End of stream", getClientInfo());
                    throw new SSLException("Buffer underflow during wrap");

                case CLOSED:
                    logger.warn("{} SSL closed, can't send data", getClientInfo());
                    closeSocket();
                    return false;
            }
        }

        return true;

    }

    /**
     * Runs any delegated tasks required by the SSLEngine during the handshake process. This is necessary to complete the SSL handshake,
     * as some steps may require additional processing that is delegated to separate tasks. This method will execute all pending tasks
     * until there are no more tasks to run.
     */
    private void runDelegatedTasks() {
        Runnable task;
        while ((task = sslEngine.getDelegatedTask()) != null) {
            task.run();
        }
    }

    /**
     * Enlarges the application buffer if the decrypted data exceeds the current buffer size. This method checks the required size for the application buffer
     * based on the SSL session and creates a new buffer if necessary, copying any existing data into it.
     *
     * @param buffer The current application ByteBuffer that may need to be enlarged.
     * @return A ByteBuffer that is large enough to hold the decrypted application data.
     */
    private ByteBuffer enlargeApplicationBuffer(ByteBuffer buffer) {
        int newSize = sslEngine.getSession().getApplicationBufferSize();
        if (newSize > buffer.capacity()) {
            ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
            buffer.flip();
            newBuffer.put(buffer);
            return newBuffer;
        } else {
            return buffer;
        }
    }

    /**
     * Enlarges the packet buffer if the encrypted data to be sent exceeds the current buffer size. This method checks the required size for the packet buffer
     * based on the SSL session and creates a new buffer if necessary, copying any existing data into it.
     *
     * @param buffer The current packet ByteBuffer that may need to be enlarged.
     * @return A ByteBuffer that is large enough to hold the encrypted data to be sent.
     */
    private ByteBuffer enlargePacketBuffer(ByteBuffer buffer) {
        int newSize = sslEngine.getSession().getPacketBufferSize();
        if (newSize > buffer.capacity()) {
            ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
            buffer.flip();
            newBuffer.put(buffer);
            return newBuffer;
        } else {
            return buffer;
        }
    }

    /**
     * Handles reading data from the session's read buffer. This method processes incoming data in stages:
     * reading the header length, reading the header, and reading the payload. It uses helper methods to
     * manage each stage and returns appropriate results based on the processing state.
     *
     * @return A SocketReadCallbackResult indicating whether to keep reading or if processing is complete.
     */
    public SocketReadCallbackResult readHandler() {
        if (!initialHandshakeDone) {
            logger.debug("{} Handshake not done, skipping read handler", getClientInfo());
            return SocketReadCallbackResult.KEEP_READING;
        }

        int availableData = readBuffer.getActiveSize();

        if (availableData == 0) {
            logger.debug("{} No application data to process yet", getClientInfo());
            return SocketReadCallbackResult.KEEP_READING;
        }

        logger.debug("{} Processing {} bytes of application data",
                getClientInfo(), readBuffer.getActiveSize());

        while (readBuffer.getActiveSize() > 0) {

            Optional<SocketReadCallbackResult> result1 =
                    partialProcessPacket(this::readHeaderLengthHandler, headerLengthBuffer);
            if (result1.isPresent()) {
                return result1.get();
            }

            Optional<SocketReadCallbackResult> result2 =
                    partialProcessPacket(this::readHeaderHandler, headerBuffer);
            if (result2.isPresent()) {
                return result2.get();
            }

            Optional<SocketReadCallbackResult> result3 =
                    partialProcessPacket(this::readDataHandler, packetBuffer);
            if (result3.isPresent()) {
                return result3.get();
            }

            headerLengthBuffer.reset();
            headerBuffer.reset();
        }

        return SocketReadCallbackResult.KEEP_READING;
    }

    /**
     * Reads the header length from the headerLengthBuffer. This method checks if there are at least 2 bytes available to read the header length,
     * and if so, it reads the length, resizes the headerBuffer accordingly, and marks the bytes as read. If there are not enough bytes to read the header length,
     * it returns false, indicating that more data is needed before processing can continue.
     *
     * @return true if the header length was successfully read and processed, false if more data is needed.
     */
    private boolean readHeaderLengthHandler() {
        if (headerLengthBuffer.getActiveSize() < 2) {
            logger.debug("{} Waiting for header length (have {} bytes)",
                    getClientInfo(), headerLengthBuffer.getActiveSize());
            return false;
        }

        byte[] lengthBytes = headerLengthBuffer.getReadPointer(2);
        int headerLength = ((lengthBytes[1] & 0xFF) << 8) | (lengthBytes[0] & 0xFF);

        logger.debug("{} Header length: {} bytes", getClientInfo(), headerLength);

        headerBuffer.resize(headerLength);
        headerLengthBuffer.readCompleted(2);
        return true;
    }

    /**
     * Reads the header from the headerBuffer. This method attempts to parse the header using the protobuf Header message.
     * If parsing is successful, it resizes the packetBuffer to the size specified in the header and returns true.
     * If parsing fails due to an InvalidProtocolBufferException, it logs an error and returns false, indicating that more data is needed or that there was a parsing error.
     *
     * @return true if the header was successfully read and processed, false if there was a parsing error or more data is needed.
     */
    private boolean readHeaderHandler() {

        if (headerBuffer.getRemainingSpace() > 0) {
            logger.debug("{} Waiting for complete header (need {} more bytes)",
                    getClientInfo(), headerBuffer.getRemainingSpace());
            return false;
        }

        try {
            Header header = Header.parseFrom(headerBuffer.toArray());

            logger.debug("{} Header parsed: service=0x{}, method={}, token={}, size={}",
                    getClientInfo(),
                    Integer.toHexString(header.getServiceHash()).toUpperCase(),
                    header.getMethodId(),
                    header.getToken(),
                    header.getSize());

            packetBuffer.resize(header.getSize());
            return true;
        } catch (InvalidProtocolBufferException e) {
            logger.error("{} Failed to parse header: {}", getClientInfo(), e.getMessage());
            return false;
        }
    }

    /**
     * Reads the data payload from the packetBuffer. This method attempts to parse the header again to extract information about the service, method, token, and size.
     * It then logs the request details and dispatches the request to the appropriate service using the ServiceDispatcher. If parsing fails due to an InvalidProtocolBufferException,
     * it logs an error and returns false, indicating that there was a parsing error with the data.
     *
     * @return true if the data was successfully read and dispatched, false if there was a parsing error.
     */
    private boolean readDataHandler() {
        if (packetBuffer.getRemainingSpace() > 0) {
            logger.debug("{} Waiting for complete payload (need {} more bytes)",
                    getClientInfo(), packetBuffer.getRemainingSpace());
            return false;
        }

        try {
            Header header = Header.parseFrom(headerBuffer.toArray());

            logger.debug("{} Request: service=0x{}, method={}, token={}, size={}",
                    getClientInfo(),
                    Integer.toHexString(header.getServiceHash()).toUpperCase(),
                    header.getMethodId(),
                    header.getToken(),
                    header.getSize());

            serviceDispatcher.dispatch(this, header.getServiceHash(),
                    header.getToken(), header.getMethodId(), packetBuffer);

            return true;
        } catch (InvalidProtocolBufferException e) {
            logger.error("{} Failed to parse data: {}", getClientInfo(), e.getMessage());
            return false;
        }
    }

    /**
     * A helper method to process a stage of packet handling. This method takes a ProcessMethod functional interface, which represents the logic for processing a specific stage
     * (e.g., reading header length, reading header, reading data). It attempts to process the stage and manages the output buffer accordingly. If the output buffer still has remaining space after processing,
     * it returns a result indicating that more data is needed. If processing fails, it closes the socket and returns a result indicating that processing should stop. If processing is successful and complete,
     * it returns an empty Optional, allowing the caller to continue to the next stage.
     *
     * @param method       The ProcessMethod representing the logic for processing a specific stage of packet handling.
     * @param outputBuffer The MessageBuffer used for storing data during this stage of processing.
     * @return An Optional containing a SocketReadCallbackResult if processing is not complete or if there was an error, or an empty Optional if processing was successful and complete.
     */
    private Optional<SocketReadCallbackResult> partialProcessPacket(
            ProcessMethod method, MessageBuffer outputBuffer) {

        if (outputBuffer.getRemainingSpace() > 0) {
            int bytesToRead = Math.min(readBuffer.getActiveSize(),
                    outputBuffer.getRemainingSpace());

            byte[] data = readBuffer.read(bytesToRead);
            outputBuffer.write(data);
        }

        if (outputBuffer.getRemainingSpace() > 0) {
            return Optional.of(SocketReadCallbackResult.KEEP_READING);
        }

        if (!method.process()) {
            closeSocket();
            return Optional.of(SocketReadCallbackResult.STOP);
        }

        return Optional.empty();
    }

    /**
     * Sends a response to the client. This method takes an authentication token and a protobuf Message as the response to be sent.
     * It constructs a header for the response, including the token, service ID, and size of the serialized message. It then creates a MessageBuffer
     * containing the header and the serialized message, and adds it to the write queue to be sent to the client.
     *
     * @param token    The authentication token associated with this response.
     * @param response The protobuf Message to be sent as a response to the client.
     */
    public void sendResponse(int token, com.google.protobuf.Message response) {
        Header.Builder headerBuilder = Header.newBuilder()
                .setToken(token)
                .setServiceId(0xFE)
                .setSize(response.getSerializedSize());

        Header header = headerBuilder.build();
        int headerSize = header.getSerializedSize();

        MessageBuffer packet = new MessageBuffer(2 + headerSize + response.getSerializedSize());
        packet.writeShortLE(headerSize);
        packet.write(header.toByteArray());
        packet.write(response.toByteArray());

        writeQueue.offer(packet);
    }

    /**
     * Sends a response to the client with a specific status code. This method is used for sending responses that do not include a protobuf message body,
     * but still need to include a header with the token and status. It constructs a header with the provided token and status, and adds it to the write queue to be sent to the client.
     *
     * @param token  The authentication token associated with this response.
     * @param status The status code to be included in the response header.
     */
    public void sendResponse(int token, int status) {
        Header.Builder headerBuilder = Header.newBuilder()
                .setToken(token)
                .setStatus(status)
                .setServiceId(0xFE);

        Header header = headerBuilder.build();
        int headerSize = header.getSerializedSize();

        MessageBuffer packet = new MessageBuffer(2 + headerSize);
        packet.writeShortLE(headerSize);
        packet.write(header.toByteArray());

        writeQueue.offer(packet);
    }

    /**
     * Checks if there is data in the write queue that needs to be sent to the client. This method is used to determine if the session has pending responses that need to be processed and sent.
     *
     * @return true if there is data in the write queue or if the SSL handshake requires a wrap operation, false otherwise.
     */
    public boolean hasDataToWrite() {
        SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
        return hsStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP || !writeQueue.isEmpty();
    }

    /**
     * Closes the socket and the SSL connection associated with this session. This method ensures that the SSL engine is properly closed before closing the underlying SocketChannel.
     * It also logs the closure of the session and handles any IOException that may occur during the closing process.
     */
    public void closeSocket() {
        if (closed) {
            return; // Prevent multiple close attempts
        }
        closed = true;

        // Log the client info before closing, since socketChannel will be closed and may not provide remote address afterward
        String clientInfo = getClientInfo();

        try {
            if (!sslEngine.isOutboundDone()) {
                sslEngine.closeOutbound();
            }
            socketChannel.close();
            logger.info("{} Session closed", clientInfo);
        } catch (IOException e) {
            logger.error("{} Error closing: {}", clientInfo, e.getMessage());
        }
    }

    /**
     * Checks if the underlying SocketChannel is still open. This is important for determining if the session is still active
     * and can be used for communication with the client.
     *
     * @return true if the SocketChannel is open, false otherwise.
     */
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    /**
     * Retrieves information about the connected client, such as its remote address.
     *
     * @return A string representation of the client's remote address, or "unknown" if it cannot be determined.
     */
    public String getClientInfo() {
        try {
            return socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            return "unknown";
        }
    }

    // Getters and setters for session state and buffers.
    public MessageBuffer getReadBuffer() {
        return readBuffer;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    /**
     * Functional interface for processing a stage of packet handling. This is used in the partialProcessPacket method
     * to allow for different processing logic at each stage (e.g., reading header length, reading header, reading payload).
     */
    @FunctionalInterface
    private interface ProcessMethod {
        boolean process();
    }
}
