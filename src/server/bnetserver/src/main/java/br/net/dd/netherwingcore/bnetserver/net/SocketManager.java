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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages socket connections for the server, including accepting new connections,
 * reading from and writing to sockets, and handling SSL encryption.
 */
public class SocketManager {

    private static final Log logger = Log.getLogger(SocketManager.class.getSimpleName());

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
        logger.debug("SSL context initialized successfully");
        logger.debug("Supported protocols: {}", String.join(", ", sslContext.getSupportedSSLParameters().getProtocols()));
        logger.debug("Supported cipher suites: {}", sslContext.getSupportedSSLParameters().getCipherSuites().length);
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
            logger.info("BnetServer listening on {}:{}", bindIp, String.valueOf(port));

            new Thread(this::run, "SocketManager-Thread").start();

            return  true;
        } catch (Exception e) {
            logger.log("Failed to start server: " + e.getMessage());
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
                        logger.debug("Selected key is invalid");
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
                        logger.error("Error handling key: {}", e.getMessage());
                        closeSession(key);
                    }
                }

                // Periodically update sessions (e.g., for timeouts or cleanup).
                updateSessions();

            } catch (IOException e) {
                logger.error("Error while reading sessions: " + e.getMessage());
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

            logger.info("{} Accepted connection from {}", getClass().getSimpleName(), clientChannel.getRemoteAddress());

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

            // Log the enabled protocols for debugging purposes.
            logger.debug("Enabled protocols: {}",
                    String.join(", ", sslEngine.getEnabledProtocols()));

            // Use cipher suites compatible with Battle.net.
            String[] preferredCiphers = {
                    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_RSA_WITH_AES_128_GCM_SHA256"
            };

            String[] supportedCiphers = sslEngine.getSupportedCipherSuites();
            List<String> enabledCiphers = new ArrayList<>();
            for (String preferred : preferredCiphers) {
                if (Arrays.asList(supportedCiphers).contains(preferred)) {
                    enabledCiphers.add(preferred);
                }
            }

            if (!enabledCiphers.isEmpty()) {
                sslEngine.setEnabledCipherSuites(enabledCiphers.toArray(new String[0]));
                logger.debug("Using cipher suites: {}", String.join(", ", enabledCiphers));
            }

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
            logger.warn("Session not found for channel");
            closeSession(key);
            return;
        }

        logger.debug("Received data from channel: " + session.getClientInfo());

        try {
            tempBuffer.clear();

            // Check how many bytes are available for reading.
            logger.debug("{} SocketManager.handleRead() called", session.getClientInfo());

            int bytesRead = session.readFromSocket();

            logger.debug("{} SocketManager.handleRead() bytes read, bytesRead: {}", session.getClientInfo(), bytesRead);

            if (bytesRead == -1) {
                // Customer disconnected
                logger.debug("{} Client disconnected during read", session.getClientInfo());
                closeSession(key);
                //return;
            }

            logger.debug("{} Read {} bytes from socket", session.getClientInfo(), bytesRead);

            if (bytesRead > 0) {
                logger.debug("{} Read {} bytes, processing...",
                        session.getClientInfo(), bytesRead);

                // Processes received data.
                SocketReadCallbackResult result = session.readHandler();

                if (result == SocketReadCallbackResult.STOP) {
                    logger.warn("{} Read handler requested stop", session.getClientInfo());
                    closeSession(key);
                    return;
                }
            }

            logger.debug("{} Read complete, bytesRead: {}, checking write queue...",
                    session.getClientInfo(), bytesRead);

            // If there is data in the write queue, register interest in WRITE.
            try {
                if (session.hasDataToWrite()) {
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else {
                    key.interestOps(SelectionKey.OP_READ); // No data to write, only interested in reading.
                }
                logger.debug("{} Updated interestOps after read, bytesRead: {}, current interestOps: {}",
                        session.getClientInfo(), bytesRead, key.interestOps());
            } catch (CancelledKeyException e) {
                // Key was canceled, session closed.
                logger.debug("{} Key cancelled, closing session", session.getClientInfo());
                closeSession(key);
            }

            logger.debug("{} Finished handleRead, bytesRead: {}, current interestOps: {}",
                    session.getClientInfo(), bytesRead, key.interestOps());

        } catch (IOException e) {
            logger.error("{} IOException during read: {}",
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
            logger.log("No session found for channel: " + channel.getRemoteAddress());
            return;
        }

        try {

            boolean allWritten = session.processWriteQueue();
            if (allWritten && !session.hasDataToWrite()) {
                key.interestOps(SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            logger.error("Error during write operation: {}", e.getMessage());
            closeSession(key);
        }
    }

    /**
     * Updates the sessions by checking for idle timeouts and closed connections,
     * removing any sessions that are no longer active.
     */
    private void updateSessions() {

        sessions.values().removeIf(session -> {
            if (!session.isOpen()) {
                logger.debug("{} Session closed, removing", session.getClientInfo());
                return true;
            }

            if (session.isIdle()) {
                logger.debug("{} Session idle for too long, closing", session.getClientInfo());
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
        Session session = null;
        SocketChannel channel = null;

        try {
            channel = (SocketChannel) key.channel();
            session = sessions.remove(channel);

            String clientInfo = (session != null) ? session.getClientInfo() :
                    (channel != null && channel.isOpen() ? channel.getRemoteAddress().toString() : "unknown");

            if (session != null) {
                session.closeSocket();
            }

            key.cancel();

            if (channel != null && channel.isOpen()) {
                channel.close();
            }

            logger.debug("{} Session closed and removed from manager", clientInfo);

        } catch (IOException e) {
            logger.error("Error closing session: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error closing session: {}", e.getMessage(), e);
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
            logger.error("Error closing server: {}", e.getMessage());
        }

        logger.info("Battle.net Service stopped, Sessions: {}", sessions.size());
    }
}
