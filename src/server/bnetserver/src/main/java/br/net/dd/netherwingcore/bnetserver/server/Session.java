package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.bnetserver.services.ServiceDispatcher;
import br.net.dd.netherwingcore.common.logging.ErrorMessage;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import br.net.dd.netherwingcore.proto.login.LoginProto;
import com.google.protobuf.Message;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Represents a client session in the Battle.net server.
 * Manages communication, authentication state, and message processing.
 */
public class Session implements Runnable {

    private final String sessionId;
    private final SSLSocket socket;
    private final Consumer<String> onClose;
    private final AtomicBoolean authenticated;
    private final ServiceDispatcher serviceDispatcher;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private LoginProto.GameAccountInfo accountInfo;

    private MessageBuffer headerLengthBuffer;
    private MessageBuffer headerBuffer;
    private MessageBuffer packetBuffer;

    /**
     * Constructs a new Session.
     *
     * @param sessionId Unique identifier for the session.
     * @param socket    The SSL socket for communication.
     * @param onClose   Callback to invoke when the session is closed.
     */
    public Session(String sessionId, SSLSocket socket, Consumer<String> onClose) {
        this.sessionId = sessionId;
        this.socket = socket;
        this.onClose = onClose;
        this.authenticated = new AtomicBoolean(false);
        this.serviceDispatcher = ServiceDispatcher.getInstance();
    }

    /**
     * Main loop for handling incoming messages from the client.
     */
    @Override
    public void run() {
        try {

            processMessage(socket.getInputStream());

        } catch (IOException e) {
            log(new ErrorMessage("Session " + sessionId + " encountered an I/O error: " + e.getMessage()));
            Arrays.stream(e.getStackTrace()).iterator().forEachRemaining((stackTraceElement) -> log(stackTraceElement.toString()));
        } finally {
            close();
        }
    }

    /**
     * Processes an incoming message from the client.
     *
     * @param data The raw message data.
     */
    private void processMessage(InputStream data) {
        try {

            MessageBuffer packet = new MessageBuffer(0x1000);
            while (packet.getActiveSize() > 0) {
                if (readHeaderLenghtHandler(headerLengthBuffer, headerBuffer)) {
                    return;
                }
            }

            RpcTypesProto.Header header = RpcTypesProto.Header.parseFrom(data);

            log("Service ID: " + header.getServiceId());
            log("Service Hash: " + String.format("0x%08X", header.getServiceHash()));
            log("Method ID: " + header.getMethodId());
            log("Token: " + header.getToken());
            log("Data " + Arrays.toString(header.toByteArray()));

            serviceDispatcher.dispatch(
                    this,
                    header.getServiceHash(),
                    header.getMethodId(),
                    header.getToken(),
                    data.readAllBytes()
            );

        } catch (Exception e) {
            log(new ErrorMessage("Session " + sessionId + " encountered an I/O error: " + e.getMessage()));
            sendError(0, BattlenetRpcErrorCode.ERROR_INTERNAL.name());
        }
    }

    /**
     * Reads the header length from the incoming message and prepares the header buffer.
     *
     * @param headerLengthBuffer Buffer to read the header length into.
     * @param headerBuffer       Buffer to prepare for reading the header.
     * @return true if the header length was successfully read and the header buffer is ready, false otherwise.
     */
    private boolean readHeaderLenghtHandler(MessageBuffer headerLengthBuffer, MessageBuffer headerBuffer) {

        // Read the first 2 bytes to determine the header length
        byte[] len = headerLengthBuffer.getReadPointer();

        // Ensure we have at least 2 bytes to read the header length
        ByteBuffer buffer = ByteBuffer.wrap(len);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Assuming little-endian format for the header length
        int headerLength = buffer.getShort() & 0xFFFF; // Convert to unsigned

        // Validate header length
        headerBuffer.resize(headerLength);

        return true;
    }

    /**
     * Sends a response message to the client.
     *
     * @param token    The token associated with the response.
     * @param response The response message to send.
     */
    public void sendResponse(int token, Message response) {
        try {

            byte[] data = response.toByteArray();

            synchronized (outputStream) {
                outputStream.writeInt(data.length);
                outputStream.write(data);
                outputStream.flush();
            }

        } catch (IOException e) {
            log(new ErrorMessage("Session " + sessionId + " encountered an I/O error: " + e.getMessage()));
            close();
        }
    }

    /**
     * Sends an error message to the client.
     *
     * @param token     The token associated with the error.
     * @param errorCode The error code to send.
     */
    public void sendError(int token, String errorCode) {
        log(new ErrorMessage("Session " + sessionId + ", token " + token + ", encountered an error: " + errorCode));
    }

    /**
     * Closes the session and releases resources.
     */
    public void close(){
        try {
            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
            onClose.accept(sessionId);
        } catch (IOException e) {
            log(new ErrorMessage("Session " + sessionId + " encountered an I/O error: " + e.getMessage()));
        }
    }

    // Getters e setters
    public String getSessionId() { return sessionId; }
    public InetAddress getRemoteAddress() { return socket.getInetAddress(); }
    public boolean isAuthenticated() { return authenticated.get(); }
    public void setAuthenticated(boolean value) { authenticated.set(value); }
    public LoginProto.GameAccountInfo getAccountInfo() { return accountInfo; }
    public void setAccountInfo(LoginProto.GameAccountInfo info) { this.accountInfo = info; }
}
