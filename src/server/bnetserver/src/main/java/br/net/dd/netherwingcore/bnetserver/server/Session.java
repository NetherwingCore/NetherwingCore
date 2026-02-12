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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Represents a client session in the BNet server. This class manages the state of a single client connection,
 * including reading and writing data, handling SSL encryption/decryption, and dispatching requests to the appropriate services.
 * Each session is associated with a SocketChannel and an SSLEngine for secure communication.
 */
public class Session {

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
    private volatile boolean handshakeComplete;
    private String accountName;
    private int accountId;

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
        this.handshakeComplete = false;

        log("New session created for " + getClientInfo());
    }

    /**
     * Starts the SSL handshake process for this session. This method will initiate the handshake and handle the
     * necessary steps to establish a secure connection with the client. If the handshake fails, the session will be closed.
     */
    public void start() {

        Log.debug("{} Starting SSL handshake", getClientInfo());

        try {
            // Starts the SSL handshake process.
            sslEngine.beginHandshake();

            Log.debug("{} Handshake status: {}",
                    getClientInfo(), String.valueOf(sslEngine.getHandshakeStatus()));

        } catch (IOException e) {
            Log.error("{} Failed to start SSL handshake: {}",
                    getClientInfo(), e.getMessage());
            closeSocket();
        }

    }

    /**
     * Handles the SSL handshake wrapping process. This method will attempt to wrap application data for the handshake and send it to the client.
     * It will manage the handshake status and handle any necessary buffer resizing if the buffers are too small to hold the data.
     * If there is an error during the wrapping process, it will log the error and close the socket.
     *
     * @throws IOException If there is an error during the wrapping process, such as issues with writing to the socket or SSL exceptions.
     */
    private void doHandshakeWrap() throws IOException {
        myNetData.clear();

        SSLEngineResult result = sslEngine.wrap(myAppData, myNetData);

        switch (result.getStatus()) {
            case OK:
                myNetData.flip();
                while (myNetData.hasRemaining()) {
                    socketChannel.write(myNetData);
                }
                Log.debug("{} Handshake WRAP: produced {} bytes",
                        getClientInfo(), String.valueOf(result.bytesProduced()));
                break;

            case BUFFER_OVERFLOW:
                myNetData = enlargeBuffer(myNetData, sslEngine.getSession().getPacketBufferSize());
                doHandshakeWrap();
                break;

            case BUFFER_UNDERFLOW:
            case CLOSED:
                Log.error("{} Handshake WRAP failed: {}",
                        getClientInfo(), String.valueOf(result.getStatus()));
                throw new SSLException("Unexpected status during wrap: " + result.getStatus());
        }

        // After wrapping, we need to check if there are any tasks that need to be run as part of the handshake process.
        // The SSLEngine may require certain tasks to be executed in order to complete the handshake, such as generating keys or performing cryptographic operations.
        // We will call the runHandshakeTasks method to execute any pending tasks before proceeding with the next steps of the handshake.
        runHandshakeTasks();
    }

    /**
     * Reads data from the socket and processes SSL decryption if needed.
     *
     * @param tempBuffer A temporary buffer for reading raw data from the socket.
     * @return The number of bytes read, or -1 if the connection is closed.
     * @throws IOException If there is an error reading from the socket or during SSL processing.
     */
    public int readFromSocket(ByteBuffer tempBuffer) throws IOException {

        // Reads raw data from the socket into the temporary buffer.
        int bytesRead = socketChannel.read(peerNetData);

        if (bytesRead == -1) {
            Log.debug("{} Connection closed by client", getClientInfo());
            closeSocket();
            return -1;
        }

        if (bytesRead == 0) {
            return 0; // No data read, but connection is still open.
        }

        Log.debug("{} Read {} bytes from socket", getClientInfo(), String.valueOf(bytesRead));

        if (!handshakeComplete) {
            doHandshake();
        } else {
            unwrapData();
        }

        return bytesRead;
    }

    /**
     * Performs the SSL handshake process by handling the necessary steps to establish a secure connection with the client.
     * This method will manage the handshake status and perform the required actions based on the current state of the handshake.
     * It will handle both wrapping and unwrapping of data as needed during the handshake process. If the handshake is successful,
     * it will set the handshakeComplete flag to true. If there is an error during the handshake, it will log the error and close the socket.
     *
     * @throws IOException If there is an error during the handshake process, such as issues with reading/writing to the socket or SSL exceptions.
     */
    private void doHandshake() throws IOException {
        peerNetData.flip(); // Prepares the buffer for reading.

        SSLEngineResult result;
        SSLEngineResult.HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();

        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED &&
                handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    result = sslEngine.unwrap(peerNetData, peerAppData);
                    handshakeStatus = result.getHandshakeStatus();

                    Log.debug("{} Handshake UNWRAP: status={}, handshakeStatus={}",
                            getClientInfo(), String.valueOf(result.getStatus()), String.valueOf(handshakeStatus));

                    switch (result.getStatus()) {
                        case OK:
                            break;

                        case BUFFER_OVERFLOW:
                            peerAppData = enlargeBuffer(peerAppData,
                                    sslEngine.getSession().getApplicationBufferSize());
                            break;

                        case BUFFER_UNDERFLOW:
                            // Need more data - compress and return
                            peerNetData.compact();
                            return;

                        case CLOSED:
                            Log.warn("{} SSL closed during handshake", getClientInfo());
                            closeSocket();
                            return;
                    }
                    break;

                case NEED_WRAP:
                    peerNetData.compact();
                    doHandshakeWrap();
                    peerNetData.clear();
                    handshakeStatus = sslEngine.getHandshakeStatus();
                    break;

                case NEED_TASK:
                    runHandshakeTasks();
                    handshakeStatus = sslEngine.getHandshakeStatus();
                    break;

                case FINISHED:
                case NOT_HANDSHAKING:
                    handshakeComplete = true;
                    Log.info("{} SSL handshake completed successfully", getClientInfo());
                    peerNetData.compact();
                    return;
            }
        }

        handshakeComplete = true;
        Log.info("{} SSL handshake finished", getClientInfo());
        peerNetData.compact();
    }

    /**
     * Runs any delegated tasks from the SSLEngine. This is necessary during the SSL handshake process, as the engine may
     * require certain tasks to be executed in order to complete the handshake. This method will check for any pending
     * tasks and execute them accordingly.
     */
    private void runHandshakeTasks() {
        Runnable task;
        while ((task = sslEngine.getDelegatedTask()) != null) {
            task.run();
        }
    }

    /**
     * Unwraps (decrypts) data received from the peer. This method processes the encrypted data in the peerNetData buffer,
     * decrypts it using the SSLEngine, and stores the decrypted data in the readBuffer for further processing.
     *
     * @throws IOException If there is an error during the unwrapping process, such as SSL exceptions.
     */
    private void unwrapData() throws IOException {
        peerNetData.flip();

        while (peerNetData.hasRemaining()) {
            peerAppData.clear();

            SSLEngineResult result = sslEngine.unwrap(peerNetData, peerAppData);

            switch (result.getStatus()) {
                case OK:
                    peerAppData.flip();
                    if (peerAppData.hasRemaining()) {
                        byte[] data = new byte[peerAppData.remaining()];
                        peerAppData.get(data);
                        readBuffer.write(data);

                        Log.debug("{} Decrypted {} bytes",
                                getClientInfo(), String.valueOf(data.length));
                    }
                    break;

                case BUFFER_OVERFLOW:
                    peerAppData = enlargeBuffer(peerAppData,
                            sslEngine.getSession().getApplicationBufferSize());
                    break;

                case BUFFER_UNDERFLOW:
                    peerNetData.compact();  // Compact and await more data.
                    return;                 // Need more data to continue unwrapping.

                case CLOSED:
                    Log.debug("{} SSL connection closed", getClientInfo());
                    closeSocket();
                    return;
            }
        }

        peerNetData.compact();
    }

    /**
     * Helper method to enlarge a ByteBuffer when it is too small to hold incoming data. This method will create a new ByteBuffer with a larger capacity,
     * copy the existing data from the old buffer to the new buffer, and return the new buffer. The new size will be either double the current capacity or the minimum required size, whichever is larger.
     *
     * @param buffer  The original ByteBuffer that needs to be enlarged.
     * @param minSize The minimum required size for the new buffer.
     * @return A new ByteBuffer with increased capacity containing the data from the original buffer.
     */
    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int minSize) {
        int newSize = Math.max(buffer.capacity() * 2, minSize);
        ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Main read handler for the session. This method is called when there is data to read from the socket.
     * It processes the incoming data in stages: first reading the header length, then the header, and finally the payload.
     *
     * @return A SocketReadCallbackResult indicating whether to keep reading or stop processing.
     */
    public SocketReadCallbackResult readHandler() {

        if (!handshakeComplete) {
            return SocketReadCallbackResult.KEEP_READING;
        }

        while (readBuffer.getActiveSize() > 0) {

            // STEP 1: Read 2 bytes with the header size.
            Optional<SocketReadCallbackResult> result1 =
                    partialProcessPacket(this::readHeaderLengthHandler, headerLengthBuffer);
            if (result1.isPresent()) {
                return result1.get();
            }

            // STEP 2: Read the Protocol Buffer header
            Optional<SocketReadCallbackResult> result2 =
                    partialProcessPacket(this::readHeaderHandler, headerBuffer);
            if (result2.isPresent()) {
                return result2.get();
            }

            // STEP 3: Read the Protocol Buffer payload
            Optional<SocketReadCallbackResult> result3 =
                    partialProcessPacket(this::readDataHandler, packetBuffer);
            if (result3.isPresent()) {
                return result3.get();
            }

            // Reset buffers for the next message.
            headerLengthBuffer.reset();
            headerBuffer.reset();
        }

        return SocketReadCallbackResult.KEEP_READING;
    }

    /**
     * Reads the first 2 bytes to determine the length of the Protocol Buffer header.
     *
     * @return true if the header length was read successfully, false if there was an error.
     */
    private boolean readHeaderLengthHandler() {
        if (headerLengthBuffer.getActiveSize() < 2) {
            return false;
        }

        byte[] lengthBytes = headerLengthBuffer.getReadPointer(2);
        int headerLength = ((lengthBytes[1] & 0xFF) << 8) | (lengthBytes[0] & 0xFF);

        Log.debug("{} Header length: {}", getClientInfo(), String.valueOf(headerLength));

        headerBuffer.resize(headerLength);
        headerLengthBuffer.readCompleted(2);
        return true;
    }

    /**
     * Reads the Protocol Buffer header and prepares the buffer for the payload.
     *
     * @return true if the header was read and parsed successfully, false if there was an error.
     */
    private boolean readHeaderHandler() {
        try {
            Header header = Header.parseFrom(headerBuffer.toArray());

            // Prepare a buffer for the payload.
            packetBuffer.resize(header.getSize());

            Log.debug("{} Header parsed: size={}", getClientInfo(), String.valueOf(header.getSize()));

            return true;

        } catch (InvalidProtocolBufferException e) {
            Log.error("{} Failed to parse header: {}", getClientInfo(), e.getMessage());
            return false;
        }
    }

    /**
     * Reads the payload data and dispatches the request to the appropriate service handler.
     *
     * @return true if the data was processed successfully, false if there was an error.
     */
    private boolean readDataHandler() {
        try {
            // Reparses the header
            Header header = Header.parseFrom(headerBuffer.toArray());

            Log.debug("{} Request: service=0x{}, method={}, token={}, size={}",
                    getClientInfo(),
                    Integer.toHexString(header.getServiceHash()).toUpperCase(),
                    String.valueOf(header.getMethodId()),
                    String.valueOf(header.getToken()),
                    String.valueOf(header.getSize()));

            // Dispatch to the correct service.
            serviceDispatcher.dispatch(
                    this,
                    header.getServiceHash(),
                    header.getToken(),
                    header.getMethodId(),
                    packetBuffer
            );

            return true;

        } catch (InvalidProtocolBufferException e) {
            Log.error("{} Failed to parse data received: {}", getClientInfo(), e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to process a packet in stages, allowing for partial reads.
     *
     * @param method       The processing method to call when the buffer is full.
     * @param outputBuffer The buffer where data should be copied to and processed.
     * @return An Optional containing a SocketReadCallbackResult if the processing should stop or keep reading,
     * or empty if the processing was successful and should continue to the next stage.
     */
    private Optional<SocketReadCallbackResult> partialProcessPacket(
            ProcessMethod method, MessageBuffer outputBuffer) {

        // Copies available data to the output buffer.
        if (outputBuffer.getRemainingSpace() > 0) {
            int bytesToRead = Math.min(
                    readBuffer.getActiveSize(),
                    outputBuffer.getRemainingSpace()
            );

            byte[] data = readBuffer.read(bytesToRead);
            outputBuffer.write(data);
        }

        // If you still need more information, keep reading.
        if (outputBuffer.getRemainingSpace() > 0) {
            return Optional.of(SocketReadCallbackResult.KEEP_READING);
        }

        // Full buffer, processes
        if (!method.process()) {
            closeSocket();
            return Optional.of(SocketReadCallbackResult.STOP);
        }

        return Optional.empty(); // Go to the next buffer.
    }

    /**
     * Sends a response back to the client with the given token and Protocol Buffer message.
     *
     * @param token    The token associated with the request, used for matching responses.
     * @param response The Protocol Buffer message to send as a response.
     */
    public void sendResponse(int token, com.google.protobuf.Message response) {
        Header.Builder headerBuilder = Header.newBuilder()
                .setToken(token)
                .setServiceId(0xFE)  // Special ID for responses
                .setSize(response.getSerializedSize());

        Header header = headerBuilder.build();
        int headerSize = header.getSerializedSize();

        // Calculate total size
        int totalSize = 2 + headerSize + response.getSerializedSize();
        MessageBuffer packet = new MessageBuffer(totalSize);

        // Writes: [2 bytes size] [protobuf header] [protobuf payload]
        packet.writeShortLE(headerSize);
        packet.write(header.toByteArray());
        packet.write(response.toByteArray());

        // Add to the write queue
        writeQueue.offer(packet);

        Log.debug("{} Queued response: token={}, size={}",
                getClientInfo(), String.valueOf(token), String.valueOf(packet.getActiveSize()));
    }

    /**
     * Sends a response back to the client with the given token and status code, without a payload.
     *
     * @param token  The token associated with the request, used for matching responses.
     * @param status The status code to send in the response header.
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

        Log.debug("{} Queued error response: token={}, status={}",
                getClientInfo(), String.valueOf(token), String.valueOf(status));

    }

    /**
     * Processes the write queue by attempting to write each packet to the socket. If a packet cannot be fully written,
     * it will remain in the queue and the method will return false to indicate that there are pending writes.
     *
     * @return true if all packets were successfully written, false if there are still pending writes in the queue.
     * @throws IOException If there is an error writing to the socket.
     */
    public boolean processWriteQueue() throws IOException {

        // If the handshake didn't complete, process it first.
        if (!handshakeComplete) {

            SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();

            if (hsStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                Log.debug("{} Handshake needs WRAP", getClientInfo());
                doHandshakeWrap();
                return false; // Keep trying
            }

            // If we are still in the handshake process but don't need to wrap, we can't send application data yet.
            return true;
        }

        // Process the write queue, attempting to write each packet to the socket.
        // If a packet cannot be fully written, it will remain in the queue for the next attempt.
        while (!writeQueue.isEmpty()) {
            MessageBuffer packet = writeQueue.peek();

            // Prepares the packet for encryption.
            // We need to copy the data from the MessageBuffer to the myAppData ByteBuffer, which is used for SSL encryption.
            // We also need to manage the position and limit of the ByteBuffer correctly to ensure that the SSL engine can read the data properly.
            myAppData.clear();
            myAppData.put(packet.toArray());
            myAppData.flip();

            // Encrypts the data using the SSLEngine.
            // The encrypted data will be stored in the myNetData ByteBuffer, which is used for writing to the socket.
            myNetData.clear();
            SSLEngineResult wrapResult = sslEngine.wrap(myAppData, myNetData);

            if (wrapResult.getStatus() != SSLEngineResult.Status.OK) {
                if (wrapResult.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    myNetData = enlargeBuffer(myNetData,
                            sslEngine.getSession().getPacketBufferSize());
                    continue;
                }
                Log.error("{} Failed to encrypt packet: {}",
                        getClientInfo(), String.valueOf(wrapResult.getStatus()));
                return false;
            }

            // Prepares the encrypted data for writing to the socket.
            myNetData.flip();

            while (myNetData.hasRemaining()) {
                int bytesWritten = socketChannel.write(myNetData);
                if (bytesWritten == 0) {
                    return false; // Socket buffer full, try again later.
                }
            }

            Log.debug("{} Sent packet: {} bytes encrypted ",
                    getClientInfo(), String.valueOf(wrapResult.bytesProduced()));

            writeQueue.poll();

        }

        return true;
    }

    /**
     * Checks if there is data in the write queue that needs to be sent to the client, or if the SSL handshake is still in progress.
     *
     * @return true if there is data to write or if the handshake is not complete, false otherwise.
     */
    public boolean hasDataToWrite() {
        return !writeQueue.isEmpty() || !handshakeComplete;
    }

    /**
     * Closes the socket channel associated with this session and logs the closure. If there is an error while closing,
     * it will be logged as well.
     */
    public void closeSocket() {
        String clientInfo = getClientInfo();
        Log.info("{} Closing socket", clientInfo);
        try {
            if (!sslEngine.isOutboundDone()) {
                sslEngine.closeOutbound();
            }
            socketChannel.close();
            Log.info("{} Session closed", clientInfo);
        } catch (IOException e) {
            Log.error("{} Error closing socket: {}", clientInfo, e.getMessage());
        }
    }

    /**
     * Checks if the socket channel associated with this session is still open.
     *
     * @return true if the socket channel is open, false otherwise.
     */
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    /**
     * Retrieves information about the client associated with this session, such as the remote address. If there is an error
     * while retrieving the information, it will return "unknown".
     *
     * @return A string containing information about the client, or "unknown" if there was an error.
     */
    public String getClientInfo() {
        try {
            return socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            return "unknown";
        }
    }

    /**
     * Gets the read buffer containing data that has been read from the socket but not yet processed.
     *
     * @return The MessageBuffer containing the data read from the socket.
     */
    public MessageBuffer getReadBuffer() {
        return readBuffer;
    }

    /**
     * Checks if the session is authenticated.
     *
     * @return true if the session is authenticated, false otherwise.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Sets the authentication status of the session.
     *
     * @param authenticated true to mark the session as authenticated, false to mark it as unauthenticated.
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * Gets the account name associated with this session.
     *
     * @return The account name as a string, or null if no account is associated.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the account name associated with this session.
     *
     * @param accountName The account name to associate with this session.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Gets the account ID associated with this session.
     *
     * @return The account ID as an integer, or 0 if no account is associated.
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Sets the account ID associated with this session.
     *
     * @param accountId The account ID to associate with this session.
     */
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
