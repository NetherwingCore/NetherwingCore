package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.bnetserver.services.ServiceDispatcher;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import br.net.dd.netherwingcore.proto.login.LoginProto;
import com.google.protobuf.Message;

import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
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
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            while (!socket.isClosed()) {

                int messageSize = inputStream.readInt();

                if (messageSize <= 0 || messageSize > 0x10000) {
                    log("Session " + sessionId + " received invalid message size: " + messageSize);
                    break;
                }

                byte[] messageData = new byte[messageSize];
                inputStream.readFully(messageData);

                processMessage(messageData);
            }

        } catch (IOException e) {
            log("Session " + sessionId + " encountered an error: " + e.getMessage());
        } finally {
            close();
        }
    }

    /**
     * Processes an incoming message from the client.
     *
     * @param data The raw message data.
     */
    private void processMessage(byte[] data) {
        try {
            RpcTypesProto.Header header = RpcTypesProto.Header.parseFrom(data);

            serviceDispatcher.dispatch(
                    this,
                    header.getServiceHash(),
                    header.getMethodId(),
                    header.getToken(),
                    data
            );

        } catch (Exception e) {
            log("Session " + sessionId + " failed to process message: " + e.getMessage());
            sendError(0, "INTERNAL_ERROR");
        }
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
            log("Session " + sessionId + " failed to send response: " + e.getMessage());
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
        // TODO: Implementar envio de erro
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
            log("Session " + sessionId + " encountered an error: " + e.getMessage());
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
