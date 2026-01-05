package br.net.dd.netherwingcore.common.banner;

import br.net.dd.netherwingcore.common.utilities.RevisionData;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class Banner {

    public static void show(String serviceName, String logFileName, String logExtraInfo){
        log(RevisionData.getFullVersion() + " (" + serviceName + ")");
        log("(<Ctrl-C> to stop.)");
        log("");
        log(" __  __          __    __                                                         ");
        log("/\\ \\/\\ \\        /\\ \\__/\\ \\                                 __                     ");
        log("\\ \\ `\\\\ \\     __\\ \\ ,_\\ \\ \\___      __   _ __   __  __  __/\\_\\    ___      __     ");
        log(" \\ \\ , ` \\  /'__`\\ \\ \\/\\ \\  _ `\\  /'__`\\/\\`'__\\/\\ \\/\\ \\/\\ \\/\\ \\ /' _ `\\  /'_ `\\   ");
        log("  \\ \\ \\`\\ \\/\\  __/\\ \\ \\_\\ \\ \\ \\ \\/\\  __/\\ \\ \\/ \\ \\ \\_/ \\_/ \\ \\ \\/\\ \\/\\ \\/\\ \\L\\ \\  ");
        log("   \\ \\_\\ \\_\\ \\____\\\\ \\__\\\\ \\_\\ \\_\\ \\____\\\\ \\_\\  \\ \\___x___/'\\ \\_\\ \\_\\ \\_\\ \\____ \\ ");
        log("    \\/_/\\/_/\\/____/ \\/__/ \\/_/\\/_/\\/____/ \\/_/   \\/__//__/   \\/_/\\/_/\\/_/\\/___L\\ \\");
        log("                                                                           /\\____/");
        log("    https://github.com/NetherwingCore/NetherwingCore                CORE   \\_/__/ ");
        log(" ");
    }

}
