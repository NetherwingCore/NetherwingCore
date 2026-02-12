package br.net.dd.netherwingcore.bnetserver.net;

import br.net.dd.netherwingcore.common.cryptography.SSLContextImpl;
import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.shared.networking.SocketReadCallbackResult;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
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

            // Configure socket options for better performance and reliability.
            clientChannel.socket().setKeepAlive(true);
            clientChannel.socket().setTcpNoDelay(true); // Disable Nagle's algorithm for lower latency.
            clientChannel.socket().setSoTimeout(0);     // No timeout, we will handle idle connections ourselves.

            Log.info("{} Accepted connection from {}", getClass().getSimpleName(), clientChannel.getRemoteAddress());

            // Create an SSL engine for the new connection.
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);  // SERVER (not client)
            sslEngine.setNeedClientAuth(false); // We can set this to true if we want to require client certificates.
            sslEngine.setWantClientAuth(false); // We can set this to true if we want to request client certificates but not require them.

            // Disable hostname verification since we're not using client certificates and this is a server-side SSL engine.
            SSLParameters sslParams = sslEngine.getSSLParameters();
            sslParams.setEndpointIdentificationAlgorithm(null);
            sslEngine.setSSLParameters(sslParams);

            // Enable only secure TLS protocols (TLSv1.2 and TLSv1.3) and disable older, less secure versions.
            sslEngine.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

            // Create a new session for the accepted connection and register it with the selector.
            Session session = new Session(clientChannel, sslEngine);
            sessions.put(clientChannel, session);

            // Register the client channel for read operations.
            clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, session);

            session.start();

        }
    }

    /**
     * Handles reading data from a client socket, processing it through the session's
     * read handler, and managing the selection key's interest ops based on whether
     * there is more data to write.
     *
     * @param key        the selection key representing the read event
     * @param tempBuffer a temporary buffer for reading data from the socket
     * @throws IOException if there is an error reading from the socket or processing the session
     */
    private void handleRead(SelectionKey key, ByteBuffer tempBuffer) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();
        Session session = sessions.get(channel);

        if (session == null) {
            Log.warn("Session not found for channel");
            closeSession(key);
            return;
        }

        Log.debug("Received data from channel: " + session.getClientInfo());

        try {
            tempBuffer.clear();

            // Check how many bytes are available for reading.
            Log.debug("{} SocketManager.handleRead() called", session.getClientInfo());

            int bytesRead = session.readFromSocket(tempBuffer);

            Log.debug("{} SocketManager.handleRead() bytes read, bytesRead: {}", session.getClientInfo(), bytesRead);

            if (bytesRead == -1) {
                // Customer disconnected
                Log.debug("{} Client disconnected during read", session.getClientInfo());
                closeSession(key);
                //return;
            }

            Log.debug("{} Read {} bytes from socket", session.getClientInfo(), bytesRead);

            if (bytesRead > 0) {
                Log.debug("{} Read {} bytes, processing...",
                        session.getClientInfo(), bytesRead);

                // Processes received data.
                SocketReadCallbackResult result = session.readHandler();

                if (result == SocketReadCallbackResult.STOP) {
                    Log.warn("{} Read handler requested stop", session.getClientInfo());
                    closeSession(key);
                    return;
                }
            }

            Log.debug("{} Read complete, bytesRead: {}, checking write queue...",
                    session.getClientInfo(), bytesRead);

            // If there is data in the write queue, register interest in WRITE.
            try {
                if (session.hasDataToWrite()) {
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else {
                    key.interestOps(SelectionKey.OP_READ); // No data to write, only interested in reading.
                }
                Log.debug("{} Updated interestOps after read, bytesRead: {}, current interestOps: {}",
                        session.getClientInfo(), bytesRead, key.interestOps());
            } catch (CancelledKeyException e) {
                // Key was canceled, session closed.
                Log.debug("{} Key cancelled, closing session", session.getClientInfo());
                closeSession(key);
            }

            Log.debug("{} Finished handleRead, bytesRead: {}, current interestOps: {}",
                    session.getClientInfo(), bytesRead, key.interestOps());

        } catch (IOException e) {
            Log.error("{} IOException during read: {}",
                    session.getClientInfo(), e.getMessage());
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
     * Updates the sessions by checking for idle timeouts and closed connections,
     * removing any sessions that are no longer active.
     */
    private void updateSessions() {
        long idleTimeout = 120000; // 2 minutos

        sessions.values().removeIf(session -> {
            if (!session.isOpen()) {
                Log.debug("{} Session closed, removing", session.getClientInfo());
                return true;
            }

            if (session.isIdle(idleTimeout)) {
                Log.info("{} Session idle for too long, closing", session.getClientInfo());
                session.closeSocket();
                return true;
            }

            return false;
        });
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
