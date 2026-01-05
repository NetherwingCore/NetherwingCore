package br.net.dd.netherwingcore.common.utilities;

import br.net.dd.netherwingcore.common.Main;

import java.awt.*;
import java.io.File;
import java.net.URISyntaxException;

public class Util {

    public static String getJarLocation() {
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile.getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
