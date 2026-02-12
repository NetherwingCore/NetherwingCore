package br.net.dd.netherwingcore.bnetserver.rest;

import br.net.dd.netherwingcore.bnetserver.rest.handlers.*;
import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.cryptography.SSLContextImpl;
import br.net.dd.netherwingcore.common.logging.Log;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * LoginRESTService is responsible for handling RESTful API requests related to login operations.
 * It sets up an HTTPS server and registers various endpoints for login, game accounts, portal information, and more.
 */
public class LoginRESTService {

    private static final Log logger = Log.getLogger(LoginRESTService.class.getSimpleName());

    private static Integer loginRestPort = 0;
    private static LoginRESTService instance = null;

    private static HttpsServer server;

    private LoginRESTService() {
        initializeService();
    }

    /**
     * Initializes the Login REST Service by creating an HTTPS server, configuring it, and registering the necessary endpoints.
     * It also sets up a thread pool for handling incoming requests.
     */
    private void initializeService() {
        loginRestPort = Config.get("LoginREST.Port", 8081);

        try {
            // Creates an HTTPS server on port 8081
            server = HttpsServer.create(new InetSocketAddress(loginRestPort), 0);
            server.setHttpsConfigurator(new HttpsConfigurator(SSLContextImpl.get()));

            logger.log("--");
            // Register endpoints
            server.createContext("/bnetserver/", new HandlerIndex());
            logger.log("Registered endpoint: /bnetserver/");

            server.createContext("/bnetserver/login/", new HandlerLogin());
            logger.log("Registered endpoint: /bnetserver/login/");

            server.createContext("/bnetserver/login/srp/", new HandlePostLoginSrpChallenge());
            logger.log("Registered endpoint: /bnetserver/login/srp/");

            server.createContext("/bnetserver/gameAccounts/", new HandlerGetGameAccounts());
            logger.log("Registered endpoint: /bnetserver/gameAccounts/");

            server.createContext("/bnetserver/portal/", new HandlerGetPortal());
            logger.log("Registered endpoint: /bnetserver/portal/");

            server.createContext("/bnetserver/refreshLoginTicket/", new HandlerPostRefreshLoginTicket());
            logger.log("Registered endpoint: /bnetserver/refreshLoginTicket/");
            logger.log("--");

            // Sets a thread pool executor for handling incoming requests
            server.setExecutor(Executors.newFixedThreadPool(4)); // Creates a thread pool with 4 threads
            // Starts the server
            server.start();

            logger.log("Login REST Service started at https://localhost:" + loginRestPort + "/bnetserver/");

        } catch (IOException e) {
            System.out.println("Failed to start Login REST Service: " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }

    }

    /**
     * Starts the Login REST Service. If the service is already running, it logs a message and does not start a new instance.
     */
    public static void start() {
        if (instance == null) {
            instance = new LoginRESTService();
        } else {

            if (server != null) {
                logger.log("Login REST Service has already been initialized at https://localhost:{}/bnetserver/", loginRestPort);
            }

        }
    }

    /**
     * Stops the Login REST Service. It stops the HTTPS server and sets the instance to null.
     */
    public static void stop() {

        server.stop(0);
        logger.log("Login REST Service stopped.");

        if (instance != null) {
            instance = null;
        }

    }

}
