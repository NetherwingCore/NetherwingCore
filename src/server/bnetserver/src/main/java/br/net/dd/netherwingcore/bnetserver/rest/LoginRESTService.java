package br.net.dd.netherwingcore.bnetserver.rest;

import br.net.dd.netherwingcore.bnetserver.rest.handlers.*;
import br.net.dd.netherwingcore.common.cache.Cache;
import br.net.dd.netherwingcore.common.cryptography.SSLContextImpl;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class LoginRESTService {

    private static Integer loginRestPort = 0;
    private static LoginRESTService instance = null;

    private static HttpsServer server;

    private LoginRESTService() {
        initializeService();
    }

    private void initializeService() {
        loginRestPort = Cache.getConfiguration().get("LoginREST.Port", 8081);

        try {
            // Creates an HTTPS server on port 8081
            server = HttpsServer.create(new InetSocketAddress(loginRestPort), 0);
            server.setHttpsConfigurator(new HttpsConfigurator(SSLContextImpl.get()));

            // Register endpoints
            server.createContext("/bnetserver/", new HandlerIndex());
            server.createContext("/bnetserver/login/", new HandlerLogin());
            server.createContext("/bnetserver/gameAccounts/", new HandlerGetGameAccounts());
            server.createContext("/bnetserver/portal/", new HandlerGetPortal());
            server.createContext("/bnetserver/refreshLoginTicket/", new HandlerPostRefreshLoginTicket());

            server.setExecutor(Executors.newFixedThreadPool(4)); // Creates a thread pool with 4 threads
            server.start();

            log("Login REST Service started at https://localhost:" + loginRestPort + "/bnetserver/");

        } catch (IOException e) {
            System.out.println("Failed to start Login REST Service: " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }

    }

    public static void start() {
        if (instance == null) {
            instance = new LoginRESTService();
        } else {

            if (server != null) {
                log("Login REST Service has already been initialized at https://localhost:" + loginRestPort + "/bnetserver/");
            }

        }
    }

    public static void stop() {

        server.stop(0);
        log("Login REST Service stopped.");

        if (instance != null) {
            instance = null;
        }

    }

}
