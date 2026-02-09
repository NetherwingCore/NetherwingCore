package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.bnetserver.services.AccountService;
import br.net.dd.netherwingcore.bnetserver.services.AuthenticationService;
import br.net.dd.netherwingcore.bnetserver.services.ConnectionService;
import br.net.dd.netherwingcore.bnetserver.services.GameUtilitiesService;
import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.cryptography.SSLContextImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.Executors;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class LoginServer {

    private static Integer battlenetPort = 0;
    private static LoginServer instance;
    private static Server server;

    public LoginServer() {
        initializeService();
    }

    private void initializeService() {

        try {
            battlenetPort = Config.get("BattlenetPort", 1119);

            server = ServerBuilder.forPort(battlenetPort)
                    .useTransportSecurity(
                            SSLContextImpl.get(SSLContextImpl.CERTIFICATE_CRT),
                            SSLContextImpl.get(SSLContextImpl.CERTIFICATE_KEY)
                    )
                    .addService(new ConnectionService())
                    .addService(new AuthenticationService())
                    .addService(new GameUtilitiesService())
                    .addService(new AccountService())
                    .intercept(new ConnectionInterceptor())
                    .executor(Executors.newFixedThreadPool(4))
                    .build();

            server.start();

            server.getServices().forEach(service -> {
                log("Service " + service.getServiceDescriptor().getName() + " started.");
            });

            server.getListenSockets().forEach(socketAddress -> {
                log("Battle.net Service listening on " + socketAddress.toString());
            });

            //log("BattleNet gRPC Server started, listening on 0.0.0.0:" + battlenetPort);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void start() {
        if (instance == null) {
            instance = new LoginServer();
        } else {
            if (server != null) {
                log("Battle.net Service has already been initialized at 0.0.0.0:" + battlenetPort);
            }
        }
    }

    public static void stop() {
        server.shutdown();
        log("Battle.net Service stopped.");

        if (instance != null) {
            instance = null;
        }
    }

}
