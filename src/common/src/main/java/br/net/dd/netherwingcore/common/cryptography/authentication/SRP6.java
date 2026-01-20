package br.net.dd.netherwingcore.common.cryptography.authentication;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class SRP6 {

    private static final BigInteger DEFAULT_N = new BigInteger("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16);
    private static final BigInteger DEFAULT_G = BigInteger.valueOf(7);

    public static class Salt {
        public static final int SALT_LENGTH = 32; // Tamanho fixo do Salt
        private final byte[] value;

        public Salt(byte[] value) {
            if (value.length != SALT_LENGTH) {
                throw new IllegalArgumentException("Salt must be exactly " + SALT_LENGTH + " bytes.");
            }
            this.value = Arrays.copyOf(value, SALT_LENGTH);
        }

        public Salt() {
            this.value = new byte[SALT_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(this.value);
        }

        public byte[] getValue() {
            return Arrays.copyOf(value, SALT_LENGTH);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (byte b : value) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    public static class SRP6Base {
        protected final BigInteger N;
        protected final BigInteger g;
        protected final BigInteger k;
        protected final BigInteger v; // Verifier
        protected final Salt s; // Fixed-size salt (32 bytes)
        protected final BigInteger b; // Private B
        protected final BigInteger B; // Public B
        protected boolean _used;

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

        private BigInteger calculatePrivateB(BigInteger N) {
            SecureRandom random = new SecureRandom();
            BigInteger b;
            do {
                b = new BigInteger(N.bitLength(), random);
            } while (b.compareTo(BigInteger.ZERO) <= 0 || b.compareTo(N.subtract(BigInteger.ONE)) >= 0);
            return b;
        }

        private BigInteger calculatePublicB(BigInteger N, BigInteger g, BigInteger k, BigInteger b, BigInteger v) {
            return g.modPow(b, N).add(v.multiply(k)).mod(N);
        }

        public BigInteger verifyClientEvidence(BigInteger A, BigInteger clientM1) {
            if (_used) {
                throw new IllegalStateException("A single SRP6 object must only ever be used to verify ONCE!");
            }
            _used = true;
            return doVerifyClientEvidence(A, clientM1);
        }

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

    public static class SRP6Client extends SRP6Base {
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
