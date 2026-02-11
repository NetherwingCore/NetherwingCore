package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.common.cryptography.SSLContextImpl;
import br.net.dd.netherwingcore.common.logging.ErrorMessage;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Manages client sessions for the Battle.net server.
 * Handles accepting new connections and maintaining active sessions.
 */
public class SessionManager {

    private final int port;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Session> sessions;
    private final AtomicBoolean running;
    private SSLServerSocket serverSocket;
    private Thread acceptThread;

    /**
     * Constructs a new SessionManager.
     *
     * @param port            The port to listen for incoming connections.
     * @param executorService The executor service for handling session tasks.
     */
    public SessionManager(int port, ExecutorService executorService) {
        this.port = port;
        this.executorService = executorService;
        this.sessions = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(false);
    }

    /**
     * Starts the SessionManager, beginning to accept new client connections.
     */
    public void start() {
        try {

            SSLServerSocketFactory sslServerSocketFactory = SSLContextImpl.get().getServerSocketFactory();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            serverSocket.bind(new InetSocketAddress(port));

            running.set(true);
            acceptThread = new Thread(this::acceptConnections, "Session-Accept-Thread");
            acceptThread.start();

            log("SessionManager started on port " + port);

        } catch (IOException e) {
            log(new ErrorMessage("Failed to start SessionManager on port " + port + e.getMessage()));
        }
    }

    /**
     * Accepts incoming client connections and creates sessions for them.
     */
    private void acceptConnections() {
        while (running.get()) {
            try {

                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                String sessionId = generateSessionId();

                Session session = new Session(sessionId, clientSocket, this::removeSession);
                sessions.put(sessionId, session);
                executorService.submit(session);

                log("New session accepted: " + sessionId + " from " + clientSocket.getRemoteSocketAddress());

            } catch (IOException e) {
                if (running.get()) {
                    log(new ErrorMessage("Failed to accept new session: " + e.getMessage()));
                }
            }
        }
    }

    /**
     * Generates a unique session ID.
     *
     * @return A unique session ID string.
     */
    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Removes a session from the active sessions map.
     *
     * @param sessionId The ID of the session to remove.
     */
    private void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log("Session removed: " + sessionId);
    }

    /**
     * Stops the SessionManager, closing all active sessions and the server socket.
     */
    public void stop() {
        running.set(false);

        sessions.values().forEach(Session::close);
        sessions.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (acceptThread != null) {
                acceptThread.interrupt();
            }
            log("SessionManager stopped.");
        } catch (IOException e) {
            log(new ErrorMessage("Failed to stop SessionManager: " + e.getMessage()));
        }
    }

}
