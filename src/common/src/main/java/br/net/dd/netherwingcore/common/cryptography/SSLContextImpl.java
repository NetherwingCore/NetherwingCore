package br.net.dd.netherwingcore.common.cryptography;

import br.net.dd.netherwingcore.common.utilities.CryptographyData;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * The SSLContextImpl class is a singleton implementation responsible for
 * initializing and providing an instance of {@link SSLContext}. This class
 * performs critical operations such as loading cryptographic data (e.g., keystore
 * and certificates) and creating SSL/TLS contexts for secure connections.
 *
 * <p>This implementation ensures the correct loading of PKCS12 keystores and
 * manages KeyManager and TrustManager initialization, providing a secure
 * SSL context for use in applications.
 * </p>
 *
 * <p>The class throws runtime exceptions for missing resources, invalid configurations,
 * or problems during initialization, ensuring all required settings and files
 * are properly loaded.</p>
 *
 * Usage Example:
 * <pre>
 *     SSLContext sslContext = SSLContextImpl.get();
 * </pre>
 */
public class SSLContextImpl {

    private static SSLContextImpl instance; // Singleton instance of SSLContextImpl

    private final SSLContext sslContext; // The initialized SSLContext instance
    private static final String KEYSTORE_PATH = "/netherwingcore_keystore.jks"; // Path to the keystore
    public static final String CERTIFICATE_CRT = "/netherwingcore.crt"; // Path to the certificate file
    public static final String CERTIFICATE_KEY = "/netherwingcore.key"; // Path to the private key file
    private static final String PROTOCOL = "TLS"; // The SSL/TLS protocol to use

    /**
     * Private constructor to initialize the SSLContext. This constructor loads
     * the specified keystore, initializes the KeyManager and TrustManager, and
     * generates the SSL/TLS context for secure communication.
     *
     * <p>Any issue while loading files, initializing cryptographic data, or processing
     * the keystore will result in a {@link RuntimeException} being thrown.</p>
     */
    private SSLContextImpl() {

        try (InputStream inputStream = SSLContextImpl.class.getResourceAsStream(KEYSTORE_PATH)) {
            if (inputStream == null) {
                throw new RuntimeException("keystore.jks file not found inside the JAR!");
            }

            // Retrieve certificate configuration details
            String certPassword = CryptographyData.getCertPassword();
            if (certPassword.isEmpty()){
                throw new RuntimeException("Certificate password is not set!");
            }

            String certAlgorithm = CryptographyData.getCertAlgorithm();
            if (certAlgorithm.isEmpty()){
                throw new RuntimeException("Certificate algorithm is not set!");
            }

            // Load the PKCS12 keystore
            char[] password = certPassword.toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(inputStream, password);

            // Initialize the KeyManagerFactory with the keystore and password
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(certAlgorithm);
            keyManagerFactory.init(keyStore, password);

            // Initialize the TrustManagerFactory with the keystore
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(certAlgorithm);
            trustManagerFactory.init(keyStore);

            // Create and initialize the SSLContext with the KeyManager and TrustManager
            sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        } catch (CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Provides the singleton instance of the SSLContext.
     *
     * @return The initialized {@link SSLContext}.
     */
    public static SSLContext get() {
        if (instance == null) {
            instance = new SSLContextImpl();
        }

        return instance.sslContext;

    }

    /**
     * Retrieves a resource file (certificate or private key) as an {@link InputStream}.
     *
     * @param file The file path to load, should be either {@link #CERTIFICATE_CRT}
     *             or {@link #CERTIFICATE_KEY}.
     * @return An {@link InputStream} of the requested file.
     * @throws RuntimeException If the specified file is not found.
     */
    public static InputStream get(String file) {
        if (file.equals(CERTIFICATE_KEY)) {
            InputStream inputStream = SSLContextImpl.class.getResourceAsStream(CERTIFICATE_KEY);
            if (inputStream == null) {
                throw new RuntimeException( CERTIFICATE_KEY + " file not found inside the JAR!");
            }
            return inputStream;
        } else if (file.equals(CERTIFICATE_CRT)) {
            InputStream inputStream = SSLContextImpl.class.getResourceAsStream(CERTIFICATE_CRT);
            if (inputStream == null) {
                throw new RuntimeException(CERTIFICATE_CRT + "keystore.jks file not found inside the JAR!");
            }
            return inputStream;
        }

        return null;
    }

}
