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

    // Protocol Buffer processing buffers
    private final MessageBuffer headerLengthBuffer;
    private final MessageBuffer headerBuffer;
    private final MessageBuffer packetBuffer;
    private final MessageBuffer readBuffer;

    // SSL/TLS buffers
    private final ByteBuffer myAppData;
    private ByteBuffer myNetData;
    private ByteBuffer peerAppData;
    private final ByteBuffer peerNetData;

    private final ConcurrentLinkedQueue<MessageBuffer> writeQueue;

    // Session state
    private volatile boolean authenticated;
    private volatile boolean initialHandshakeDone;
    private volatile boolean closed;
    private volatile long lastActivityTime;
    private volatile long handshakeCompletedTime;

    private String accountName;
    private int accountId;

    private static final long IDLE_TIMEOUT_MS = 30000; // 30 seconds
    private static final long POST_HANDSHAKE_TIMEOUT_MS = 5000; // 5 seconds

    /**
     * Constructs a new Session for a given SocketChannel and SSLEngine.
     *
     * @param socketChannel The SocketChannel associated with this session.
     * @param sslEngine     The SSLEngine for handling SSL encryption/decryption for this session.
     */
    public Session(SocketChannel socketChannel, SSLEngine sslEngine) {

        logger.setDebugEnabled(true);

        this.socketChannel = socketChannel;
        this.sslEngine = sslEngine;
        this.serviceDispatcher = ServiceDispatcher.getInstance();

        this.headerLengthBuffer = new MessageBuffer(2);
        this.headerBuffer = new MessageBuffer();
        this.packetBuffer = new MessageBuffer();
        this.readBuffer = new MessageBuffer(16384);

        // Allocate SSL buffers according to SSLSession
        int appBufferSize = sslEngine.getSession().getApplicationBufferSize();
        int netBufferSize = sslEngine.getSession().getPacketBufferSize();

        this.myAppData = ByteBuffer.allocate(appBufferSize);
        this.myNetData = ByteBuffer.allocate(netBufferSize);
        this.peerAppData = ByteBuffer.allocate(appBufferSize);
        this.peerNetData = ByteBuffer.allocate(netBufferSize);

        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.authenticated = false;
        this.initialHandshakeDone = false;
        this.closed = false;
        this.lastActivityTime = System.currentTimeMillis();
        this.handshakeCompletedTime = -1;

        logger.info("New session created for {}", getClientInfo());
    }

    /**
     * Starts the SSL handshake process for this session. This method will initiate the handshake and handle the
     * necessary steps to establish a secure connection with the client. If the handshake fails, the session will be closed.
     */
    public void start() {

        try {
            logger.debug("{} Starting SSL handshake", getClientInfo());
            sslEngine.beginHandshake();
            logger.debug("{} Handshake status: {}", getClientInfo(), sslEngine.getHandshakeStatus());
        } catch (SSLException e) {
            logger.error("{} Failed to initiate handshake: {}",
                    getClientInfo(), e.getMessage(), e);
            closeSocket();
        }

    }

    /**
     * Reads data from the socket and processes it through the SSL engine. This method handles reading encrypted data from the network,
     * decrypting it using the SSLEngine, and storing the decrypted application data in the read buffer. It also manages the SSL handshake state
     * and logs relevant information about the connection and received data. If the peer closes the connection or if there is an SSL error, this method will handle those cases appropriately.
     *
     * @return The number of bytes read from the socket, or -1 if the connection was closed by the peer.
     * @throws IOException If an I/O error occurs during reading from the socket or processing SSL data.
     */
    public int readFromSocket() throws IOException {
        // Read encrypted data from network
        int bytesRead = socketChannel.read(peerNetData);

        if (bytesRead > 0) {
            lastActivityTime = System.currentTimeMillis();
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
                            getClientInfo(), e.getMessage(), e);
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

                        // Check if handshake completed (during unwrap)
                        if (!initialHandshakeDone &&
                                (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED ||
                                        result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)) {
                            markHandshakeComplete();
                        }

                        // If we have application data, store it
                        if (peerAppData.hasRemaining()) {
                            byte[] data = new byte[peerAppData.remaining()];
                            peerAppData.get(data);
                            readBuffer.write(data);

                            logger.info("╔══════════════════════════════════════════════════════");
                            logger.info("║ {} 📦 Received Application Data", getClientInfo());
                            logger.info("╠══════════════════════════════════════════════════════");
                            logger.info("║ Size: {} bytes", data.length);

                            if (logger.isDebugEnabled() && data.length > 0) {
                                logger.info("║ Hex dump:");
                                logger.info("║   {}", formatHexDump(data, 128));
                            }

                            logger.info("╚══════════════════════════════════════════════════════");
                        }
                        break;

                    case BUFFER_OVERFLOW:
                        // Need larger app buffer
                        peerAppData = enlargeApplicationBuffer(peerAppData);
                        break;

                    case BUFFER_UNDERFLOW:
                        // Need more network data
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
            logger.trace("{} No data available to read", getClientInfo());
            return 0;

        } else { // bytesRead < 0
            if (initialHandshakeDone && handshakeCompletedTime > 0) {
                long timeSinceHandshake = System.currentTimeMillis() - handshakeCompletedTime;
                if (readBuffer.getActiveSize() == 0) {
                    logger.warn("╔══════════════════════════════════════════════════════");
                    logger.warn("║ {} ❌ Client Disconnected", getClientInfo());
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
                logger.warn("{} Client disconnected before handshake completed",
                        getClientInfo());
            }
            closeSocket();
            return -1;
        }

        return bytesRead;
    }

    /**
     * Marks the SSL handshake as complete and logs relevant information about the established SSL session.
     * This method is called when the handshake process finishes successfully,
     * and it retrieves details from the SSLSession to log information such as the protocol, cipher suite, session ID, and peer certificates.
     * It also logs that the session is ready to receive application data from the client.
     */
    private void markHandshakeComplete() {
        initialHandshakeDone = true;
        handshakeCompletedTime = System.currentTimeMillis();

        SSLSession sslSession = sslEngine.getSession();
        logger.info("╔══════════════════════════════════════════════════════");
        logger.info("║ {} ✅ SSL Handshake Completed", getClientInfo());
        logger.info("╠══════════════════════════════════════════════════════");
        logger.info("║ Protocol:      {}", sslSession.getProtocol());
        logger.info("║ Cipher Suite:  {}", sslSession.getCipherSuite());
        logger.info("║ Session ID:    {}",
                bytesToHex(sslSession.getId()).substring(0,
                        Math.min(32, bytesToHex(sslSession.getId()).length())));
        logger.info("║ Valid:         {}", sslSession.isValid());

        try {
            var peerCerts = sslSession.getPeerCertificates();
            logger.info("║ Peer Certs:    {} certificate(s)", peerCerts.length);
        } catch (Exception e) {
            logger.info("║ Peer Certs:    None (server doesn't require client cert)");
        }

        logger.info("╚══════════════════════════════════════════════════════");
        logger.info("{} ⏳ Ready to receive application data from WoW client...",
                getClientInfo());
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

        if (!initialHandshakeDone && hsStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
            logger.trace("{} Handshake needs WRAP", getClientInfo());

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

                // Check if handshake completed (during wrap)
                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED ||
                        result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                    markHandshakeComplete();
                }
            }

            return false; // Keep trying
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

                    logger.trace("{} Sent encrypted packet: {} bytes",
                            getClientInfo(), result.bytesProduced());

                    writeQueue.poll(); // Successfully sent
                    break;

                case BUFFER_OVERFLOW:
                    myNetData = enlargePacketBuffer(myNetData);
                    break;

                case BUFFER_UNDERFLOW:
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
            logger.trace("{} Handshake not done, skipping read handler", getClientInfo());
            return SocketReadCallbackResult.KEEP_READING;
        }

        int availableData = readBuffer.getActiveSize();

        if (availableData == 0) {
            logger.trace("{} No application data to process yet", getClientInfo());
            return SocketReadCallbackResult.KEEP_READING;
        }

        logger.debug("{} 🔄 Processing {} bytes of application data",
                getClientInfo(), availableData);

        while (readBuffer.getActiveSize() > 0) {

            // STEP 1: Read 2-byte header length
            Optional<SocketReadCallbackResult> result1 =
                    partialProcessPacket(this::readHeaderLengthHandler, headerLengthBuffer);
            if (result1.isPresent()) {
                return result1.get();
            }

            // STEP 2: Read Protocol Buffer header
            Optional<SocketReadCallbackResult> result2 =
                    partialProcessPacket(this::readHeaderHandler, headerBuffer);
            if (result2.isPresent()) {
                return result2.get();
            }

            // STEP 3: Read Protocol Buffer payload
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

        // ✅ BIG-ENDIAN (Battle.net protocol)
        int headerLength = ((lengthBytes[0] & 0xFF) << 8) | (lengthBytes[1] & 0xFF);

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
            byte[] headerData = headerBuffer.toArray();

            // ✅ Log of header bytes before parsing.
            if (logger.isDebugEnabled()) {
                logger.debug("{} Header bytes ({}): {}",
                        getClientInfo(), headerData.length, bytesToHex(headerData));
            }

            Header header = Header.parseFrom(headerData);

            logger.debug("{} Header parsed: service=0x{}, method={}, token={}, size={}",
                    getClientInfo(),
                    Integer.toHexString(header.getServiceHash()).toUpperCase(),
                    header.getMethodId(),
                    header.getToken(),
                    header.getSize());

            packetBuffer.resize(header.getSize());
            return true;
        } catch (InvalidProtocolBufferException e) {
            logger.error("{} Failed to parse header: {}", getClientInfo(), e.getMessage(), e);

            // ✅ Full buffet logo in case of error.
            byte[] errorData = headerBuffer.toArray();
            logger.error("{} Header buffer content (hex): {}",
                    getClientInfo(), bytesToHex(errorData));

            return false;
        } catch (Exception e) {
            logger.error("{} Unexpected error parsing header: {}",
                    getClientInfo(), e.getMessage(), e);
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

            if (bytesToRead > 0) {
                byte[] data = readBuffer.read(bytesToRead);

                // ✅ Log in before writing.
                logger.trace("{} Writing {} bytes to buffer (capacity={}, limit={}, writePos={}, remaining={})",
                        getClientInfo(), data.length,
                        outputBuffer.getCapacity(),
                        outputBuffer.getLimit(),
                        outputBuffer.getWritePos(),
                        outputBuffer.getRemainingSpace());

                try {
                    outputBuffer.write(data);
                } catch (Exception e) {
                    logger.error("{} Error writing to buffer: {}",
                            getClientInfo(), e.getMessage(), e);
                    logger.error("{} Buffer state: capacity={}, writePos={}, dataLength={}",
                            getClientInfo(),
                            outputBuffer.getCapacity(),
                            outputBuffer.getWritePos(),
                            data.length);
                    throw e;
                }
            }
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

    /**
     * Checks if the session has been idle for longer than the defined timeout period.
     * This method considers both the time since the last activity and the time since the handshake was completed.
     * If the handshake is completed but no data has been received, it uses a shorter timeout to determine idleness.
     * This helps to identify clients that connect but do not send any data after the handshake.
     *
     * @return true if the session is considered idle, false otherwise.
     */
    public boolean isIdle() {
        long now = System.currentTimeMillis();

        // If handshake completed but no data received, use short timeout
        if (initialHandshakeDone && handshakeCompletedTime > 0) {
            long timeSinceHandshake = now - handshakeCompletedTime;
            if (timeSinceHandshake > POST_HANDSHAKE_TIMEOUT_MS && readBuffer.getActiveSize() == 0) {
                logger.warn("{} No data received {}ms after handshake completion",
                        getClientInfo(), timeSinceHandshake);
                return true;
            }
        }

        // General timeout
        return (now - lastActivityTime) > IDLE_TIMEOUT_MS;
    }

    /**
     * Converts a byte array to a hexadecimal string representation. This is useful for logging binary data in a human-readable format.
     *
     * @param bytes The byte array to be converted to hex.
     * @return A string containing the hexadecimal representation of the input byte array.
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    /**
     * Formats a byte array into a hex dump string for logging purposes. This method formats the byte array in a structured way, showing the hexadecimal values of the bytes.
     * It also limits the output to a specified maximum number of bytes, and indicates if there are additional bytes that were not included in the dump.
     *
     * @param data     The byte array to be formatted as a hex dump.
     * @param maxBytes The maximum number of bytes to include in the hex dump. If the data exceeds this length, it will indicate how many additional bytes are not shown.
     * @return A string containing the formatted hex dump of the input byte array.
     */
    private String formatHexDump(byte[] data, int maxBytes) {
        StringBuilder sb = new StringBuilder();
        int length = Math.min(data.length, maxBytes);

        for (int i = 0; i < length; i++) {
            if (i > 0 && i % 16 == 0) {
                sb.append("\n║   ");
            } else if (i > 0 && i % 8 == 0) {
                sb.append("  ");
            }
            sb.append(String.format("%02X ", data[i]));
        }

        if (data.length > maxBytes) {
            sb.append("... (").append(data.length - maxBytes).append(" more bytes)");
        }

        return sb.toString();
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
