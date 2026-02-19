package br.net.dd.netherwingcore.common.utilities;

import br.net.dd.netherwingcore.common.GitRevision;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The {@code CryptographyData} class provides utility methods for accessing cryptographic data
 * such as certificate passwords, aliases, and algorithms. These properties are loaded
 * from a configuration file located in the classpath under the resource name {@code cryptography.properties}.
 *
 * <p>The properties file is expected to contain the following keys:
 * <ul>
 *     <li>{@code cert.password} - The password for the cryptographic certificate.</li>
 *     <li>{@code cert.alias} - The alias associated with the certificate.</li>
 *     <li>{@code cert.algorithm} - The algorithm used for cryptographic operations.</li>
 * </ul>
 *
 * <p>This class loads the properties file at class initialization (static block) and
 * throws a {@link RuntimeException} if the file cannot be read or loaded.
 */
public class CryptographyData {

    // Holds the properties loaded from the cryptography configuration file.
    private static final Properties props = new Properties();

    static {
        try (InputStream in = GitRevision.class.getResourceAsStream("/cryptography.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load revision data.", e);
        }
    }

    /**
     * Retrieves the password for the cryptographic certificate.
     *
     * @return the certificate password as a {@link String}, or {@code null} if the property is not found.
     */
    public static String getCertPassword(){
        return props.getProperty("cert.password");
    }

    /**
     * Retrieves the alias associated with the cryptographic certificate.
     *
     * @return the certificate alias as a {@link String}, or {@code null} if the property is not found.
     */
    public static String getCertAlias(){
        return props.getProperty("cert.alias");
    }

    /**
     * Retrieves the algorithm used for cryptographic operations.
     *
     * @return the cryptographic algorithm as a {@link String}, or {@code null} if the property is not found.
     */
    public static String getCertAlgorithm(){
        return props.getProperty("cert.algorithm");
    }

}
