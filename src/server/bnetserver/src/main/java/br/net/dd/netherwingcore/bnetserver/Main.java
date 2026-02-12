package br.net.dd.netherwingcore.bnetserver;


import br.net.dd.netherwingcore.bnetserver.configuration.BnetConfigSample;
import br.net.dd.netherwingcore.bnetserver.net.SocketManager;
import br.net.dd.netherwingcore.bnetserver.rest.LoginRESTService;
import br.net.dd.netherwingcore.common.banner.Banner;
import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.logging.LogFile;

public class Main {

    static SocketManager socketManager;
    static Log logger;

    static void main() {

        Config.loadConfig(new BnetConfigSample());

        logger = Log.getLogger(Main.class.getSimpleName());

        Banner.show("NetherwingCore BNet Server", "bnetserver.log", "");

        logger.log("Loading configuration...");

        LoginRESTService.start();
        socketManager = new SocketManager();

        if (socketManager.start(
                Config.get("BindIP", "0.0.0.0"),
                Config.get("BattlenetPort", 1119)
        )) {
            logger.log("NetherwingCore BNet Server started successfully.");
        } else {
            logger.log("Failed to start NetherwingCore BNet Server.");
            return;
        }

        logger.info("NetherwingCore BNet Server is running", new LogFile("bnetserver.log"));


        // Adds a hook to capture Ctrl+C.
        Runtime.getRuntime().addShutdownHook(new Thread(Main::stopServices));

    }

    public static void stopServices() {
        LoginRESTService.stop();
        socketManager.stop();
        logger.info("NetherwingCore BNet Server stopped successfully.", new  LogFile("bnetserver.log"));
    }

}
