package br.net.dd.netherwingcore.bnetserver;


import br.net.dd.netherwingcore.bnetserver.configuration.BnetConfigSample;
import br.net.dd.netherwingcore.bnetserver.rest.LoginRESTService;
import br.net.dd.netherwingcore.bnetserver.server.SessionManager;
import br.net.dd.netherwingcore.common.banner.Banner;
import br.net.dd.netherwingcore.common.configuration.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class Main {

    static SessionManager sessionManager;

    static void main() {

        Banner.show("NetherwingCore BNet Server", "bnetserver.log", "");

        log("Loading configuration...");

        Config.loadConfig(new BnetConfigSample());

        LoginRESTService.start();

        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );

        sessionManager = new SessionManager(
                Config.get("BattlenetPort", 1119),
                executorService
        );
        sessionManager.start();

        // Adds a hook to capture Ctrl+C.
        Runtime.getRuntime().addShutdownHook(new Thread(Main::stopServices));

    }

    public static void stopServices() {
        log("Stopping NetherwingCore BNet Server...");
        sessionManager.stop();
        LoginRESTService.stop();
    }

}
