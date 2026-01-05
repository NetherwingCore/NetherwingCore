package br.net.dd.netherwingcore.common.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CryptographyData {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = RevisionData.class.getResourceAsStream("/cryptography.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load revision data.", e);
        }
    }

    public static String getCertPassword(){
        return props.getProperty("cert.password");
    }

    public static String getCertAlias(){
        return props.getProperty("cert.alias");
    }

    public static String getCertAlgorithm(){
        return props.getProperty("cert.algorithm");
    }

}
