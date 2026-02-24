package br.net.dd.netherwingcore.common.cryptography;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;

import java.util.Optional;

/**
 * Utility class for hashing and verifying passwords using the Argon2 algorithm.
 */
public final class Argon2Hash
{
    public static final int HASH_LEN = 16;                     // bytes (128 bits)
    public static final int ENCODED_HASH_LEN = 100;            // chars
    public static final int DEFAULT_ITERATIONS = 10;
    public static final int DEFAULT_MEMORY_COST = (1 << 17);   // KiB (~128 MiB)
    public static final int PARALLELISM = 1;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Argon2Hash() {}

    /**
     * Hashes a password using the Argon2 algorithm with the provided salt and default parameters.
     *
     * @param password  The password to hash.
     * @param saltBytes The salt to use for hashing.
     * @return An Optional containing the encoded hash if successful, or an empty Optional if an error occurs.
     */
    public static Optional<String> hash(String password, byte[] saltBytes)
    {
        return hash(password, saltBytes, DEFAULT_ITERATIONS, DEFAULT_MEMORY_COST);
    }

    /**
     * Hashes a password using the Argon2 algorithm with the provided parameters.
     *
     * @param password        The password to hash.
     * @param saltBytes       The salt to use for hashing.
     * @param nIterations     The number of iterations to perform.
     * @param kibMemoryCost   The memory cost in KiB.
     * @return An Optional containing the encoded hash if successful, or an empty Optional if an error occurs.
     */
    public static Optional<String> hash(String password, byte[] saltBytes, int nIterations, int kibMemoryCost)
    {
        try
        {
            // Create an Argon2 function instance with the specified parameters
            Argon2Function fn = Argon2Function.getInstance(
                    kibMemoryCost,
                    nIterations,
                    PARALLELISM,
                    HASH_LEN,
                    Argon2.ID
            );

            Hash h = Password.hash(password)
                    .addSalt(saltBytes)
                    .with(fn);

            String encoded = h.getResult();

            // Validate the encoded hash length to ensure it is within expected limits
            if (encoded == null || encoded.length() >= ENCODED_HASH_LEN)
                return Optional.empty();

            return Optional.of(encoded);
        }
        catch (Exception e)
        {
            return Optional.empty();
        }
    }

    /**
     * Verifies a password against an encoded hash.
     *
     * @param password    The password to verify.
     * @param encodedHash The encoded hash to compare against.
     * @return True if the password matches the hash, false otherwise.
     */
    public static boolean verify(String password, String encodedHash)
    {
        try
        {
            // Create an Argon2 function instance based on the parameters encoded in the hash
            Argon2Function fn = Argon2Function.getInstanceFromHash(encodedHash);
            return Password.check(password, encodedHash).with(fn);

            // The above line is equivalent to:
            // return Password.check(password, encodedHash).withArgon2();

        }
        catch (Exception e)
        {
            return false;
        }
    }
}
