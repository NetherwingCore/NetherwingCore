package br.net.dd.netherwingcore.bnetserver.server;

import com.google.rpc.context.AttributeContext;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.*;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Handles communication with a single client over an SSL socket.
 * This class is designed to be run in a separate thread for each client connection.
 */
public class ClientHandler implements Runnable {

    private final SSLSocket clientSocket;

    /**
     * Constructs a new ClientHandler for the given SSL socket.
     *
     * @param clientSocket The SSL socket connected to the client.
     */
    public ClientHandler(SSLSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * The main method that runs the client handler thread.
     * It reads messages from the client, processes them, and sends responses back.
     * The communication protocol is simple: the client sends a message length followed by the message data.
     * The server echoes back the same message to the client.
     */
    @Override
    public void run() {
        // Use try-with-resources to ensure streams are closed properly
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            log("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            log("All Bytes: "+in.readAllBytes().length);

        } catch (IOException e) {
            log("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                log("Connection closed with client.");
            } catch (IOException e) {
                log("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
