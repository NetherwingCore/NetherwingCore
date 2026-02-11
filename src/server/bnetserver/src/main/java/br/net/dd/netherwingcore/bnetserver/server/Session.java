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

    // Buffers for SSL encryption/decryption (if needed).
    private ByteBuffer sslNetBuffer;      // Encrypted data read from the socket
    private ByteBuffer sslAppBuffer;      // Decrypted application data
    private ByteBuffer sslOutNetBuffer;   // Data to send (encrypted)
    private ByteBuffer sslOutAppBuffer;   // Data to send (decrypted)

    // Queue for outgoing messages to be written to the socket.
    private final ConcurrentLinkedQueue<MessageBuffer> writeQueue;

    // Authentication state and account information.
    private volatile boolean authenticated;
    private volatile HandshakeState handshakeState;
    private String accountName;
    private int accountId;

    /** Enum to represent the state of the SSL handshake process. This can be used to track the progress of the handshake
     * and handle different stages accordingly. For example, you might want to log different messages or take specific
     * actions based on whether the handshake is in progress, completed, or failed.
     */
    private enum HandshakeState {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

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
        this.sslNetBuffer = ByteBuffer.allocate(netBufferSize);
        this.sslAppBuffer = ByteBuffer.allocate(appBufferSize);
        this.sslOutNetBuffer = ByteBuffer.allocate(netBufferSize);
        this.sslOutAppBuffer = ByteBuffer.allocate(appBufferSize);

        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.authenticated = false;

        log("New session created for " + getClientInfo());
    }

    /**
     * Starts the SSL handshake process for this session. This method will initiate the handshake and handle the
     * necessary steps to establish a secure connection with the client. If the handshake fails, the session will be closed.
     */
    public void start() {

        try {
            // Starts the SSL handshake process.
            sslEngine.beginHandshake();
            handshakeState = HandshakeState.IN_PROGRESS;

            // Performs the handshake steps, which may involve multiple rounds of communication with the client.
            performHandshakeWrap();

        } catch (IOException e) {
            Log.error("{} Failed to start SSL handshake: {}", getClientInfo(), e.getMessage());
            handshakeState = HandshakeState.FAILED;
            closeSocket();
        }

    }

    /**
     * Performs the SSL handshake wrap step, which involves generating handshake data to send to the client.
     * This method will call sslEngine.wrap() to create the necessary handshake messages and write them to the socket.
     * If the handshake is completed successfully, it will update the handshake state accordingly.
     *
     * @throws IOException If there is an error during the SSL handshake process, such as writing to the socket.
     */
    private void performHandshakeWrap() throws IOException {

        sslOutAppBuffer.clear();
        sslOutNetBuffer.clear();

        SSLEngineResult wrapResult = sslEngine.wrap(sslOutAppBuffer, sslOutNetBuffer);

        if (wrapResult.bytesProduced() > 0) {
            sslOutNetBuffer.flip();

            while (sslOutNetBuffer.hasRemaining()) {
                socketChannel.write(sslOutNetBuffer);
            }

            Log.debug("{} Sent {} bytes during handshake", getClientInfo(), String.valueOf(wrapResult.bytesProduced()));
        }

        if (wrapResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            handshakeState = HandshakeState.COMPLETED;
            Log.info("{} SSL Handshake completed", getClientInfo());
        }

    }

    /**
     * Reads data from the socket and processes SSL decryption if needed.
     *
     * @param tempBuffer A temporary buffer for reading raw data from the socket.
     * @return The number of bytes read, or -1 if the connection is closed.
     * @throws IOException If there is an error reading from the socket or during SSL processing.
     */
    public int readFromSocket(ByteBuffer tempBuffer) throws IOException {

        // If the handshake is still in progress, we need to handle SSL decryption.
        if (handshakeState == HandshakeState.FAILED) {
            return -1;
        }

        // Reads raw data from the socket into the temporary buffer.
        int bytesRead = socketChannel.read(sslNetBuffer);

        if (bytesRead == -1) {
            Log.debug("{} Connection closed by client", getClientInfo());
            return -1;
        }

        if (bytesRead == 0) {
            return 0; // No data read, but connection is still open.
        }

        Log.debug("{} Read {} bytes from socket", getClientInfo(), String.valueOf(bytesRead));

        if (handshakeState == HandshakeState.IN_PROGRESS) {
            processHandshake();
            return bytesRead;
        }

        if (handshakeState == HandshakeState.COMPLETED) {
            unwrapApplicationData();
        }

        return bytesRead;
    }

    /**
     * Processes the SSL handshake by unwrapping incoming handshake data and handling the different stages of the handshake process.
     * This method will continue to unwrap data until the handshake is completed or if there is an error. It will also handle any
     * necessary tasks that need to be run during the handshake, such as delegated tasks from the SSLEngine.
     *
     * @throws IOException If there is an error during the SSL handshake process, such as issues with unwrapping or writing to the socket.
     */
    private void processHandshake() throws IOException {
        sslNetBuffer.flip();

        // We need to unwrap the incoming handshake data until we have processed all of it or until the handshake is completed.
        while (sslNetBuffer.hasRemaining() && handshakeState == HandshakeState.IN_PROGRESS) {
            sslAppBuffer.clear();

            SSLEngineResult unwrapResult = sslEngine.unwrap(sslAppBuffer, sslNetBuffer);

            Log.debug("{} Handshake unwrap: status={}, handshakeStatus={}, bytesConsumed={}, bytesProduced={}",
                    getClientInfo(),
                    String.valueOf(unwrapResult.getStatus()),
                    String.valueOf(unwrapResult.getHandshakeStatus()),
                    String.valueOf(unwrapResult.bytesConsumed()),
                    String.valueOf(unwrapResult.bytesProduced()));

            // After unwrapping, we need to check the result status to determine the next steps in the handshake process.
            switch (unwrapResult.getStatus()) {
                case OK:
                    // If the handshake is finished, we can proceed to the next steps.
                    break;

                case BUFFER_UNDERFLOW:
                    // Need more data, break to read more from the socket.
                    sslNetBuffer.compact();
                    return;

                case BUFFER_OVERFLOW:
                    // This should not happen during handshake unwrap, but if it does, we need to resize the application buffer.
                    int appSize = sslEngine.getSession().getApplicationBufferSize();
                    sslAppBuffer = ByteBuffer.allocate(appSize);
                    break;

                case CLOSED:
                    Log.warn("{} SSL closed during handshake", getClientInfo());
                    handshakeState = HandshakeState.FAILED;
                    return;
            }

            // After processing the unwrap result, we need to check the handshake status to determine if we need to run any delegated tasks or if the handshake is completed.
            SSLEngineResult.HandshakeStatus handshakeStatus = unwrapResult.getHandshakeStatus();

            switch (handshakeStatus) {
                case NEED_TASK:
                    // If the handshake status indicates that we need to run delegated tasks, we will execute those tasks before continuing with the handshake process.
                    // This is necessary because the SSLEngine may require certain tasks to be performed in order to complete the handshake, such as generating keys or performing cryptographic operations.
                    runDelegatedTasks();
                    break;

                case NEED_WRAP:
                    // If the handshake status indicates that we need to wrap data to send to the client, we will call the performHandshakeWrap method to generate the necessary handshake messages and send them to the client.
                    sslNetBuffer.compact();
                    performHandshakeWrap();
                    sslNetBuffer.clear();
                    break;

                case NEED_UNWRAP:
                    // Continue unwrapping in the next iteration.
                    break;

                case FINISHED:
                    handshakeState = HandshakeState.COMPLETED;
                    Log.info("{} SSL Handshake completed", getClientInfo());
                    break;

                case NOT_HANDSHAKING:
                    handshakeState = HandshakeState.COMPLETED;
                    Log.info("{} SSL Handshake completed (NOT_HANDSHAKING)", getClientInfo());
                    break;

            }
        }
        sslNetBuffer.compact();
    }

    /**
     * Runs any delegated tasks from the SSLEngine. This is necessary during the SSL handshake process, as the engine may
     * require certain tasks to be executed in order to complete the handshake. This method will check for any pending
     * tasks and execute them accordingly.
     */
    private void runDelegatedTasks() {
        Runnable task;
        while ((task = sslEngine.getDelegatedTask()) != null) {
            task.run();
        }
    }

    /**
     * Unwraps application data from the SSL engine. This method is called after the SSL handshake is completed and is responsible for
     * decrypting incoming data from the client. It will read encrypted data from the sslNetBuffer, unwrap it using the sslEngine,
     * and store the decrypted application data in the readBuffer for further processing by the read handler.
     *
     * @throws IOException If there is an error during the unwrapping process, such as issues with buffer sizes or SSL exceptions.
     */
    private void unwrapApplicationData() throws IOException {
        sslNetBuffer.flip();

        SSLEngineResult unwrapResult = sslEngine.unwrap(sslAppBuffer, sslNetBuffer);

        switch (unwrapResult.getStatus()) {
            case OK:
                // If the unwrap was successful, we need to flip the sslAppBuffer to prepare it for reading and then copy the decrypted data into the readBuffer for processing by the read handler.
                sslAppBuffer.flip();
                if (sslAppBuffer.hasRemaining()) {
                    byte[] data = new byte[sslAppBuffer.remaining()];
                    sslAppBuffer.get(data);
                    readBuffer.write(data);

                    Log.debug("{} Decrypted {} bytes", getClientInfo(), String.valueOf(data.length));
                }
                break;

            case BUFFER_OVERFLOW:
                // This means our application buffer is too small to hold the decrypted data. We need to resize it based on the session's requirements.
                int appSize = sslEngine.getSession().getApplicationBufferSize();
                ByteBuffer newAppBuffer = ByteBuffer.allocate(appSize + sslAppBuffer.position());
                sslAppBuffer.flip();
                newAppBuffer.put(sslAppBuffer);
                sslAppBuffer = newAppBuffer;
                break;

            case BUFFER_UNDERFLOW:
                // Need more data, break to read more from the socket.
                sslNetBuffer.compact();
                break;

            case CLOSED:
                Log.warn("{} SSL closed during data unwrap", getClientInfo());
                closeSocket();
                break;
        }
    }

    /**
     * Main read handler for the session. This method is called when there is data to read from the socket.
     * It processes the incoming data in stages: first reading the header length, then the header, and finally the payload.
     *
     * @return A SocketReadCallbackResult indicating whether to keep reading or stop processing.
     */
    public SocketReadCallbackResult readHandler() {
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
        byte[] lengthBytes = headerLengthBuffer.getReadPointer(2);
        int headerLength = ((lengthBytes[1] & 0xFF) << 8) | (lengthBytes[0] & 0xFF);

        headerBuffer.resize(headerLength);
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
            return true;

        } catch (InvalidProtocolBufferException e) {
            log("Failed to parse header: " + e.getMessage());
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

            log(getClientInfo() +
                    " received request service_hash=" + header.getServiceHash() +
                    " method_id=" + header.getMethodId() +
                    " token=" + header.getToken() +
                    " size=" + header.getSize());

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
            log("Failed to parse header in data handler: " + e.getMessage());
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

        return Optional.empty(); // Vai para próximo buffer
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
    }

    /**
     * Processes the write queue by attempting to write each packet to the socket. If a packet cannot be fully written,
     * it will remain in the queue and the method will return false to indicate that there are pending writes.
     *
     * @return true if all packets were successfully written, false if there are still pending writes in the queue.
     * @throws IOException If there is an error writing to the socket.
     */
    public boolean processWriteQueue() throws IOException {
        while (!writeQueue.isEmpty()) {
            MessageBuffer packet = writeQueue.peek();

            ByteBuffer buffer = ByteBuffer.wrap(packet.toArray());
            int bytesWritten = socketChannel.write(buffer);

            if (bytesWritten < packet.getActiveSize()) {
                // If you couldn't write everything, try again in the next iteration.
                return false;
            }

            writeQueue.poll(); // Remove da fila
        }

        return true;
    }

    /**
     * Closes the socket channel associated with this session and logs the closure. If there is an error while closing,
     * it will be logged as well.
     */
    public void closeSocket() {
        try {
            socketChannel.close();
            log("Session closed for " + getClientInfo());
        } catch (IOException e) {
            log("Error closing socket: " + e.getMessage());
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
