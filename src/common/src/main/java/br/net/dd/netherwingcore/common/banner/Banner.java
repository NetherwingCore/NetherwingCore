package br.net.dd.netherwingcore.common.banner;

import br.net.dd.netherwingcore.common.utilities.RevisionData;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class Banner {

    public static void show(String serviceName, String logFileName, String logExtraInfo){
        log(RevisionData.getFullVersion() + " (" + serviceName + ")");
        log("(<Ctrl-C> to stop.)");
        log("");
        log(" ███▄    █ ▓█████▄▄▄█████▓ ██░ ██ ▓█████  ██▀███   ▄████▄   ▒█████   ██▀███  ▓█████ ");
        log(" ██ ▀█   █ ▓█   ▀▓  ██▒ ▓▒▓██░ ██▒▓█   ▀ ▓██ ▒ ██▒▒██▀ ▀█  ▒██▒  ██▒▓██ ▒ ██▒▓█   ▀ ");
        log("▓██  ▀█ ██▒▒███  ▒ ▓██░ ▒░▒██▀▀██░▒███   ▓██ ░▄█ ▒▒▓█    ▄ ▒██░  ██▒▓██ ░▄█ ▒▒███   ");
        log("▓██▒  ▐▌██▒▒▓█  ▄░ ▓██▓ ░ ░▓█ ░██ ▒▓█  ▄ ▒██▀▀█▄  ▒▓▓▄ ▄██▒▒██   ██░▒██▀▀█▄  ▒▓█  ▄ ");
        log("▒██░   ▓██░░▒████▒ ▒██▒ ░ ░▓█▒░██▓░▒████▒░██▓ ▒██▒▒ ▓███▀ ░░ ████▓▒░░██▓ ▒██▒░▒████▒");
        log("░ ▒░   ▒ ▒ ░░ ▒░ ░ ▒ ░░    ▒ ░░▒░▒░░ ▒░ ░░ ▒▓ ░▒▓░░ ░▒ ▒  ░░ ▒░▒░▒░ ░ ▒▓ ░▒▓░░░ ▒░ ░");
        log("░ ░░   ░ ▒░ ░ ░  ░   ░     ▒ ░▒░ ░ ░ ░  ░  ░▒ ░ ▒░  ░  ▒     ░ ▒ ▒░   ░▒ ░ ▒░ ░ ░  ░");
        log("   ░   ░ ░    ░    ░       ░  ░░ ░   ░     ░░   ░ ░        ░ ░ ░ ▒    ░░   ░    ░   ");
        log("         ░    ░  ░         ░  ░  ░   ░  ░   ░     ░ ░          ░ ░     ░        ░  ░");
        log("https://github.com/NetherwingCore/NetherwingCore  ░                                 ");
        log(" ");
    }

}
