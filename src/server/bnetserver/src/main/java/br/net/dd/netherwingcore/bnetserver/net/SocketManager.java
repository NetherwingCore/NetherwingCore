package br.net.dd.netherwingcore.bnetserver.net;

import br.net.dd.netherwingcore.common.cryptography.SSLContextImpl;
import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.shared.networking.SocketReadCallbackResult;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Manages socket connections for the server, including accepting new connections,
 * reading from and writing to sockets, and handling SSL encryption.
 */
public class SocketManager {

    private final Map<SocketChannel, Session> sessions;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private SSLContext sslContext;
    private volatile boolean running;

    /**
     * Constructs a new SocketManager instance.
     */
    public SocketManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.running = false;
    }

    /**
     * Initializes the SSL context for secure communication.
     *
     * @throws Exception if there is an error initializing the SSL context
     */
    private void initSSL() throws Exception {
        sslContext = SSLContextImpl.get();
        Log.info("SSL context initialized successfully");
    }

    /**
     * Starts the socket server on the specified IP and port.
     *
     * @param bindIp the IP address to bind to
     * @param port   the port number to listen on
     * @return true if the server started successfully, false otherwise
     */
    public boolean start(String bindIp, int port) {

        try {
            // Initialize SSL context before starting the server.
            initSSL();

            // Open the selector and server socket channel.
            selector = Selector.open();

            bindIp = bindIp.replace("\"", ""); // Sanitize the bind IP by removing any quotes.
            InetSocketAddress socketAddress = new InetSocketAddress(bindIp, port);

            // Configure the server socket channel for non-blocking mode and bind it to the specified address.
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(socketAddress);
            serverChannel.register(selector, java.nio.channels.SelectionKey.OP_ACCEPT);

            running = true;
            Log.info("BnetServer listening on {}:{}", bindIp, String.valueOf(port));

            new Thread(this::run, "SocketManager-Thread").start();

            return  true;
        } catch (Exception e) {
            log("Failed to start server: " + e.getMessage());
            return false;
        }

    }

    /**
     * Main loop for handling socket events, including accepting new connections,
     * reading from and writing to sockets, and updating session states.
     */
    private void run() {
        ByteBuffer tempBuffer = ByteBuffer.allocate(8192);

        while (running) {
            try {
                // Wait for events with a timeout to allow periodic session updates.
                int readyChannels = selector.select(50);

                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        Log.debug("Selected key is invalid");
                        continue;
                    }

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isReadable()) {
                            handleRead(key, tempBuffer);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    } catch (Exception e) {
                        Log.error("Error handling key: {}", e.getMessage());
                        closeSession(key);
                    }
                }

                // Periodically update sessions (e.g., for timeouts or cleanup).
                updateSessions();

            } catch (IOException e) {
                Log.error("Error while reading sessions: " + e.getMessage());
            }
        }
    }

    /**
     * Handles accepting a new client connection, setting up SSL, and registering
     * the new session with the selector.
     *
     * @param key the selection key representing the accept event
     * @throws IOException if there is an error accepting the connection or setting up SSL
     */
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);

            Log.info("Accepted connection from " + clientChannel.getRemoteAddress());

            // Create an SSL engine for the new connection.
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);  // SERVER (not client)
            sslEngine.setNeedClientAuth(false); // We can set this to true if we want to require client certificates.

            // Create a new session for the accepted connection and register it with the selector.
            Session session = new Session(clientChannel, sslEngine);
            sessions.put(clientChannel, session);

            // Register the client channel for read operations.
            clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, session);

            session.start();

        }
    }

    /**
     * Handles reading data from a client socket, processing it through the session,
     * and managing the session's state based on the read results.
     *
     * @param key        the selection key representing the read event
     * @param tempBuffer a temporary buffer for reading data from the socket
     * @throws IOException if there is an error reading from the socket or processing the session
     */
    private void handleRead(SelectionKey key, ByteBuffer tempBuffer) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Session session = sessions.get(channel);

        if (session == null) {
            Log.warn("No session found for channel: {}", String.valueOf(channel.getRemoteAddress()));
            closeSession(key);
            return;
        }

        tempBuffer.clear();
        int bytesRead = session.readFromSocket(tempBuffer);

        if (bytesRead == -1) {
            // Client has closed the connection.
            Log.debug("{} Client disconnected", session.getClientInfo());
            closeSession(key);
            return;
        }

        if (bytesRead > 0) {
            // Process the read data and invoke the session's read handler.
            SocketReadCallbackResult result = session.readHandler();

            if (result == SocketReadCallbackResult.STOP){
                closeSession(key);
                return;
            }
        }

        try {

            if (session.hasDataToWrite()) {
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } else {
                key.interestOps(SelectionKey.OP_READ);
            }

        } catch (CancelledKeyException e) {
            Log.warn("Selection key was cancelled while processing read: {}", e.getMessage());
            closeSession(key);
        }

    }

    /**
     * Handles writing data to a client socket based on the session's write queue,
     * and manages the selection key's interest ops based on whether there are
     * more writes to process.
     *
     * @param key the selection key representing the write event
     * @throws IOException if there is an error writing to the socket or processing the session
     */
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Session session = sessions.get(channel);

        if (session == null) {
            log("No session found for channel: " + channel.getRemoteAddress());
            return;
        }

        try {

            boolean allWritten = session.processWriteQueue();
            if (allWritten && !session.hasDataToWrite()) {
                key.interestOps(SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            Log.error("Error during write operation: {}", e.getMessage());
            closeSession(key);
        }
    }

    /**
     * Periodically updates the sessions, removing any that are no longer open.
     * This method can be expanded to include additional session management tasks
     * such as handling timeouts or performing cleanup.
     */
    private void updateSessions() {
        // Remove any sessions that are no longer open.
        sessions.values().removeIf(session -> !session.isOpen());
    }

    /**
     * Closes a session associated with the given selection key, removing it from
     * the sessions map and canceling the selection key.
     *
     * @param key the selection key representing the session to close
     */
    private void closeSession(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            Session session = sessions.remove(channel);

            if (session != null) {
                session.closeSocket();
            }

            key.cancel();
            channel.close();
        } catch (IOException e) {
            Log.error("Error closing session: {}", e.getMessage());
        }
    }

    /**
     * Stops the socket server, closing all sessions and the server channel.
     * This method ensures that all resources are properly released and logs
     * the shutdown process.
     */
    public void stop() {
        running = false;

        // Close all sessions and the server channel.
        sessions.values().forEach(Session::closeSocket);
        sessions.clear();

        try {

            if (selector != null) {
                selector.close();
            }

            if (serverChannel != null) {
                serverChannel.close();
            }

        } catch (IOException e) {
            Log.error("Error closing server: {}", e.getMessage());
        }

        log("BnetServer stopped.");
    }
}
