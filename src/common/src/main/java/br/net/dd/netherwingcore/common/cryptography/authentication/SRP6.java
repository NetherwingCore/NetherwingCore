package br.net.dd.netherwingcore.common.cryptography.authentication;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * SRP6 (Secure Remote Password protocol version 6) implementation for cryptographic operations.
 * Provides a secure way to authenticate users without transmitting their passwords over the network.
 * Includes computation of SRP parameters, verifier, client evidence verification, and other utilities.
 *
 * The protocol is implemented based on the following components:
 * - {@link SRP6.Salt}: Class to handle fixed-length salt generation and representation.
 * - {@link SRP6.SRP6Base}: Base class for handling SRP6 computations and protocol workflows.
 * - {@link SRP6.SRP6Client}: Specialized implementation of the SRP6 protocol for a client.
 *
 * <p>This implementation uses the default SRP parameters:
 * {@code N} (prime modulus) and {@code g} (generator) for the cryptographic operations.
 * The {@code SHA-1} hashing algorithm is used for cryptographic hashing.</p>
 */
public class SRP6 {

    /**
     * Default SRP prime modulus (N), defined as a 1024-bit safe prime constant.
     */
    private static final BigInteger DEFAULT_N = new BigInteger("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16);

    /**
     * Default SRP generator (g), defined as the integer value 7.
     */
    private static final BigInteger DEFAULT_G = BigInteger.valueOf(7);

    /**
     * Represents a fixed-length salt used in SRP computations.
     * The salt is critical for protecting against brute-force attacks by being incorporated into hash calculations.
     */
    public static class Salt {
        public static final int SALT_LENGTH = 32; // Fixed salt length in bytes.
        private final byte[] value;

        /**
         * Constructs a Salt instance using a provided fixed-length byte array.
         *
         * @param value A byte array of exactly 32 bytes.
         * @throws IllegalArgumentException if the provided byte array is not of the required length.
         */
        public Salt(byte[] value) {
            if (value.length != SALT_LENGTH) {
                throw new IllegalArgumentException("Salt must be exactly " + SALT_LENGTH + " bytes.");
            }
            this.value = Arrays.copyOf(value, SALT_LENGTH);
        }

        /**
         * Constructs a Salt instance by generating random bytes of a fixed length.
         * Uses a secure random number generator.
         */
        public Salt() {
            this.value = new byte[SALT_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(this.value);
        }

        /**
         * Returns the value of the salt as a byte array.
         *
         * @return A copy of the internal salt byte array.
         */
        public byte[] getValue() {
            return Arrays.copyOf(value, SALT_LENGTH);
        }

        /**
         * Converts the salt value into a hexadecimal string representation.
         *
         * @return A hexadecimal representation of the salt.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (byte b : value) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    /**
     * Base class for SRP6 computations. Handles the protocol's mathematical operations
     * with parameters such as prime modulus (N), generator (g), salt, and verifier.
     * Provides methods for private and public values and client evidence verification.
     */
    public static class SRP6Base {

        protected final BigInteger N;        // Prime modulus
        protected final BigInteger g;        // Generator
        protected final BigInteger k;        // Multiplier parameter
        protected final BigInteger v;        // Verifier
        protected final Salt s;              // Fixed-size salt
        protected final BigInteger b;        // Private value for the server
        protected final BigInteger B;        // Public value for the server
        protected boolean _used;             // Indicates if this instance has been used

        /**
         * Constructs an SRP6Base instance with the provided parameters.
         *
         * @param i        An unused initialization parameter (hash of username may be passed here).
         * @param salt     The salt used in SRP computation.
         * @param verifier The verifier generated during the registration phase.
         * @param N        Prime modulus for SRP computations.
         * @param g        Generator for SRP computations.
         * @param k        Multiplier parameter.
         */
        public SRP6Base(BigInteger i, Salt salt, BigInteger verifier, BigInteger N, BigInteger g, BigInteger k) {
            this.N = N;
            this.g = g;
            this.k = k;
            this.v = verifier;
            this.s = salt;
            this.b = calculatePrivateB(N);
            this.B = calculatePublicB(N, g, k, b, v);
            this._used = false;
        }

        /**
         * Generates a cryptographically strong private value (b).
         *
         * @param N The SRP modulus.
         * @return A private random value (b).
         */
        private BigInteger calculatePrivateB(BigInteger N) {
            SecureRandom random = new SecureRandom();
            BigInteger b;
            do {
                b = new BigInteger(N.bitLength(), random);
            } while (b.compareTo(BigInteger.ZERO) <= 0 || b.compareTo(N.subtract(BigInteger.ONE)) >= 0);
            return b;
        }

        /**
         * Calculates the public value (B) for the server.
         *
         * @param N The SRP modulus.
         * @param g The SRP generator.
         * @param k The SRP multiplier parameter.
         * @param b The private value (b).
         * @param v The password verifier.
         * @return Public value (B).
         */
        private BigInteger calculatePublicB(BigInteger N, BigInteger g, BigInteger k, BigInteger b, BigInteger v) {
            return g.modPow(b, N).add(v.multiply(k)).mod(N);
        }

        /**
         * Verifies the client's evidence (M1) based on the SRP protocol.
         *
         * @param A Public value sent by the client.
         * @param clientM1 Client's proof of knowledge of the shared key.
         * @return The session key (K) if verification succeeds; null otherwise.
         * @throws IllegalStateException if the object is reused.
         */
        public BigInteger verifyClientEvidence(BigInteger A, BigInteger clientM1) {
            if (_used) {
                throw new IllegalStateException("A single SRP6 object must only ever be used to verify ONCE!");
            }
            _used = true;
            return doVerifyClientEvidence(A, clientM1);
        }

        // Helper methods for hashing and cryptographic computations ...
        protected BigInteger doVerifyClientEvidence(BigInteger A, BigInteger clientM1) {
            if (A.mod(N).equals(BigInteger.ZERO)) {
                return null;
            }

            BigInteger u = calculateU(A);
            BigInteger S = A.multiply(v.modPow(u, N)).modPow(b, N);
            BigInteger K = hashInterleave(S);

            BigInteger NgHash = hashXor(hash(N), hash(g));
            BigInteger ourM = hash(new BigInteger[]{NgHash, new BigInteger(s.getValue()), A, B, K});

            if (ourM.equals(clientM1)) {
                return K;
            }

            return null;
        }

        private BigInteger calculateU(BigInteger A) {
            return hash(new BigInteger[]{A, B});
        }

        private BigInteger hash(BigInteger... values) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                for (BigInteger value : values) {
                    digest.update(value.toByteArray());
                }
                return new BigInteger(1, digest.digest());
            } catch (Exception e) {
                throw new RuntimeException("Failed hashing", e);
            }
        }

        private BigInteger hashXor(BigInteger hash1, BigInteger hash2) {
            byte[] h1 = hash1.toByteArray();
            byte[] h2 = hash2.toByteArray();
            byte[] result = new byte[Math.max(h1.length, h2.length)];

            for (int i = 0; i < result.length; i++) {
                byte a = (i < h1.length) ? h1[h1.length - 1 - i] : 0;
                byte b = (i < h2.length) ? h2[h2.length - 1 - i] : 0;
                result[result.length - 1 - i] = (byte) (a ^ b);
            }

            return new BigInteger(1, result);
        }

        private BigInteger hashInterleave(BigInteger S) {
            byte[] sBytes = S.toByteArray();
            byte[] evenChunks = new byte[sBytes.length / 2];
            byte[] oddChunks = new byte[sBytes.length / 2];

            for (int i = 0; i < sBytes.length; i++) {
                if (i % 2 == 0) {
                    evenChunks[i / 2] = sBytes[i];
                } else {
                    oddChunks[i / 2] = sBytes[i];
                }
            }

            BigInteger hashEven = hash(new BigInteger[]{new BigInteger(1, evenChunks)});
            BigInteger hashOdd = hash(new BigInteger[]{new BigInteger(1, oddChunks)});

            byte[] result = new byte[hashEven.toByteArray().length + hashOdd.toByteArray().length];

            for (int i = 0; i < result.length / 2; i++) {
                result[2 * i] = hashEven.toByteArray()[i];
                result[2 * i + 1] = hashOdd.toByteArray()[i];
            }

            return new BigInteger(1, result);
        }
    }

    /**
     * Represents an SRP-6 client that performs authentication using a username and password.
     * Derived from {@link SRP6Base}.
     */
    public static class SRP6Client extends SRP6Base {

        /**
         * Constructs an SRP-6 client with the given username, password, and salt.
         *
         * @param username The user's username.
         * @param password The user's password.
         * @param salt     The salt used to hash the verifier.
         * @param verifier The verifier computed from the username, password, and salt.
         */
        public SRP6Client(String username, String password, Salt salt, BigInteger verifier) {
            super(hash(username), salt, verifier, DEFAULT_N, DEFAULT_G, BigInteger.valueOf(3));
        }

        private static BigInteger hash(String input) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                return new BigInteger(1, digest.digest(input.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException("Failed hashing", e);
            }
        }
    }

}
