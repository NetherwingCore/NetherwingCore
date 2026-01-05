package br.net.dd.netherwingcore.bnetserver;


import br.net.dd.netherwingcore.bnetserver.configuration.BnetConfigSample;
import br.net.dd.netherwingcore.bnetserver.rest.LoginRESTService;
import br.net.dd.netherwingcore.common.banner.Banner;
import br.net.dd.netherwingcore.common.cache.Cache;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class Main {
    static void main() {

        Banner.show("NetherwingCore BNet Server", "bnetserver.log", "");

        log("Loading configuration...");

        Cache.loadConfig(new BnetConfigSample());

        LoginRESTService.start();

        // Adds a hook to capture Ctrl+C.
        Runtime.getRuntime().addShutdownHook(new Thread(Main::stopServices));

    }

    public static void stopServices() {
        log("Stopping NetherwingCore BNet Server...");
        LoginRESTService.stop();
    }

}
