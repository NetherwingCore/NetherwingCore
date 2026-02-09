package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.cryptography.SSLContextImpl;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class BattleNetService {

    private static Integer battlenetPort = 0;

    private static volatile boolean running = false;

    private static SSLServerSocket server = null;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static BattleNetService instance;

    private BattleNetService() {
        initializeService();
    }

    private void initializeService() {

        try {
            battlenetPort = Config.get("BattlenetPort", 1119);
            SSLServerSocketFactory sslServerSocketFactory = SSLContextImpl.get().getServerSocketFactory();
            server = (SSLServerSocket) sslServerSocketFactory.createServerSocket(battlenetPort);

            running = true;

            log("Battle.net Service started at port 0.0.0.0:" + battlenetPort);

            Thread acceptThread = new Thread(() -> {
                while (running) {
                    try {
                        SSLSocket clientSocket = (SSLSocket) server.accept();
                        executor.submit(new ClientHandler(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            log("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            });
            acceptThread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void start() {
        if (instance == null) {
            instance = new BattleNetService();
        } else {
            if (server != null) {
                log("Battle.net  Service has already been initialized at port " + battlenetPort);
            }
        }
    }

    public static void stop() {
        running = false;
        if (server != null) {
            try {
                server.close();
                log("Battle.net Service stopped.");
            } catch (IOException e) {
                log("Error stopping Battle.net Service: " + e.getMessage());
            }
        }
        executor.shutdownNow();
        instance = null;
    }

}
