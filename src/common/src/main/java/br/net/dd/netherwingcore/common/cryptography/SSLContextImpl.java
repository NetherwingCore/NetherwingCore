package br.net.dd.netherwingcore.common.cryptography;

import br.net.dd.netherwingcore.common.utilities.CryptographyData;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLContextImpl {

    private static SSLContextImpl instance;

    private final SSLContext sslContext;
    private static final String KEYSTORE_PATH = "/netherwingcore_keystore.jks";
    public static final String CERTIFICATE_CRT = "/netherwingcore.crt";
    public static final String CERTIFICATE_KEY = "/netherwingcore.key";
    private static final String PROTOCOL = "TLS";

    private SSLContextImpl() {

        try (InputStream inputStream = SSLContextImpl.class.getResourceAsStream(KEYSTORE_PATH)) {
            if (inputStream == null) {
                throw new RuntimeException("keystore.jks file not found inside the JAR!");
            }

            String certPassword = CryptographyData.getCertPassword();
            if (certPassword.isEmpty()){
                throw new RuntimeException("Certificate password is not set!");
            }

            String certAlgorithm = CryptographyData.getCertAlgorithm();
            if (certAlgorithm.isEmpty()){
                throw new RuntimeException("Certificate algorithm is not set!");
            }

            // Load the generated PKCS12 file.
            char[] password = certPassword.toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(inputStream, password);

            // Initialize KeyManager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(certAlgorithm);
            keyManagerFactory.init(keyStore, password);

            // Initialize TrustManager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(certAlgorithm);
            trustManagerFactory.init(keyStore);

            //Create SSL context
            sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        } catch (CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }

    }

    public static SSLContext get() {
        if (instance == null) {
            instance = new SSLContextImpl();
        }

        return instance.sslContext;

    }

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
