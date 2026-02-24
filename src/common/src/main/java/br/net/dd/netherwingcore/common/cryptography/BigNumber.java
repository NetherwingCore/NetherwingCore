package br.net.dd.netherwingcore.common.cryptography;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * A BigNumber class that mimics the behavior of OpenSSL's BIGNUM,
 * with constructors and methods for setting values from various formats, performing arithmetic operations,
 * and converting to/from byte arrays and strings. This class is immutable and thread-safe.
 */
public final class BigNumber {
    private static final SecureRandom RNG = new SecureRandom();

    private BigInteger bn;

    /**
     * Default constructor initializes to zero.
     */
    public BigNumber() {
        this.bn = BigInteger.ZERO;
    }

    /**
     * Copy constructor: since BigInteger is immutable, we can safely share the reference.
     * @param other the BigNumber to copy
     */
    public BigNumber(BigNumber other) {
        this.bn = other.bn; // BigInteger is immutable, so sharing is safe
    }

    /**
     * Note: Java has no unsigned int, so we use long for the constructor to allow values up to 0xFFFFFFFF.
     * @param vUnsigned32 the unsigned 32-bit integer value to set; will be treated as unsigned, so values above 0x7FFFFFFF will produce positive BigNumbers
     */
    public BigNumber(long vUnsigned32) { // equivalent to BigNumber(uint32)
        this();
        setDwordUnsigned(vUnsigned32);
    }

    /**
     * Note: Java has no unsigned int, so we use long for the constructor to allow values up to 0xFFFFFFFF.
     * @param vSigned32 the signed 32-bit integer value to set; will be treated as signed, so negative values will produce negative BigNumbers
     */
    public BigNumber(int vSigned32) { // equivalent to BigNumber(int32)
        this();
        setDword(vSigned32);
    }

    /**
     * @param hex the hexadecimal string to set; may optionally start with "0x" or "0X"; will be treated as a positive number regardless of leading zeros; must not be null or empty after trimming
     */
    public BigNumber(String hex) {
        this();
        setHexStr(hex);
    }

    public BigNumber(byte[] binary, boolean littleEndian) {
        this();
        setBinary(binary, littleEndian);
    }

    public void setDword(int val) {
        // Note: abs(Integer.MIN_VALUE) overflows in int; use long
        long abs = Math.abs((long) val);
        setDwordUnsigned(abs);
        if (val < 0) {
            this.bn = this.bn.negate();
        }
    }

    public void setDwordUnsigned(long val) {
        // interpret as uint32
        long u32 = val & 0xFFFF_FFFFL;
        this.bn = BigInteger.valueOf(u32);
    }

    public void setQword(long valUnsigned64) {
        // BigInteger has no direct unsigned long, so convert to 8 big-endian bytes
        byte[] be = new byte[8];
        long v = valUnsigned64;
        for (int i = 7; i >= 0; i--) {
            be[i] = (byte) (v & 0xFF);
            v >>>= 8;
        }
        this.bn = new BigInteger(1, be);
    }

    public void setBinary(byte[] bytes, boolean littleEndian) {
        if (bytes == null) {
            this.bn = BigInteger.ZERO;
            return;
        }
        byte[] data = bytes;
        if (littleEndian) {
            data = reverseCopy(bytes);
        }
        // OpenSSL BN_bin2bn / BN_lebin2bn treat input as magnitude (unsigned)
        this.bn = new BigInteger(1, data);
    }

    public boolean setDecStr(String str) {
        if (str == null) return false;
        String s = str.trim();
        if (s.isEmpty()) return false;
        try {
            this.bn = new BigInteger(s, 10);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean setHexStr(String str) {
        if (str == null) return false;
        String s = str.trim();
        if (s.isEmpty()) return false;

        // OpenSSL BN_hex2bn accepts optional "0x" prefix. We'll accept it too.
        if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }
        if (s.isEmpty()) return false;

        try {
            // BN_hex2bn typically interprets as magnitude; we keep it positive here.
            this.bn = new BigInteger(s, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setRand(int numBits) {
        if (numBits <= 0) {
            this.bn = BigInteger.ZERO;
            return;
        }

        // bottom=1 in OpenSSL forces the least significant bit = 1 (odd number).
        // top=0 does not force the most significant bit.
        BigInteger r = new BigInteger(numBits, RNG);
        if (!r.testBit(0)) {
            r = r.setBit(0);
        }
        this.bn = r;
    }

    public BigNumber assign(BigNumber other) {
        if (this == other) return this;
        this.bn = other.bn;
        return this;
    }

    public BigNumber addAssign(BigNumber other) {
        this.bn = this.bn.add(other.bn);
        return this;
    }

    public BigNumber subAssign(BigNumber other) {
        this.bn = this.bn.subtract(other.bn);
        return this;
    }

    public BigNumber mulAssign(BigNumber other) {
        this.bn = this.bn.multiply(other.bn);
        return this;
    }

    public BigNumber divAssign(BigNumber other) {
        this.bn = this.bn.divide(other.bn);
        return this;
    }

    public BigNumber modAssign(BigNumber other) {
        // OpenSSL BN_nnmod => result is always non-negative
        this.bn = this.bn.mod(other.bn);
        return this;
    }

    public BigNumber shiftLeftAssign(int n) {
        this.bn = this.bn.shiftLeft(n);
        return this;
    }

    // non-mutable "operators" (equivalent to overloads returning a copy)
    public BigNumber add(BigNumber other) { return new BigNumber(this).addAssign(other); }
    public BigNumber sub(BigNumber other) { return new BigNumber(this).subAssign(other); }
    public BigNumber mul(BigNumber other) { return new BigNumber(this).mulAssign(other); }
    public BigNumber div(BigNumber other) { return new BigNumber(this).divAssign(other); }
    public BigNumber mod(BigNumber other) { return new BigNumber(this).modAssign(other); }
    public BigNumber shiftLeft(int n) { return new BigNumber(this).shiftLeftAssign(n); }

    public int compareTo(BigNumber other) {
        return this.bn.compareTo(other.bn);
    }

    public boolean isZero() {
        return this.bn.signum() == 0;
    }

    public boolean isNegative() {
        return this.bn.signum() < 0;
    }

    public BigNumber exp(BigNumber exponent) {
        // BigInteger.pow accepts only int; OpenSSL accepts an arbitrary-size exponent.
        // So we implement fast exponentiation with a BigInteger (non-negative) exponent.
        if (exponent.bn.signum() < 0) {
            throw new ArithmeticException("Negative exponent not supported (matches typical integer exponent rules).");
        }
        BigInteger result = BigInteger.ONE;
        BigInteger base = this.bn;
        BigInteger e = exponent.bn;

        while (e.signum() > 0) {
            if (e.testBit(0)) {
                result = result.multiply(base);
            }
            base = base.multiply(base);
            e = e.shiftRight(1);
        }

        BigNumber ret = new BigNumber();
        ret.bn = result;
        return ret;
    }

    public BigNumber modExp(BigNumber exponent, BigNumber modulus) {
        BigNumber ret = new BigNumber();
        ret.bn = this.bn.modPow(exponent.bn, modulus.bn);
        return ret;
    }

    public int getNumBytes() {
        if (bn.signum() == 0) return 0;
        // magnitude bytes
        return toUnsignedBigEndianMagnitude(bn).length;
    }

    public int getNumBits() {
        return bn.bitLength();
    }

    public long asDwordUnsigned() {
        return bn.longValue() & 0xFFFF_FFFFL;
    }

    /**
     * Writes the unsigned big-endian (or little-endian) byte representation of this BigNumber into the provided buffer.
     * The number is treated as unsigned, and will be padded with leading zeros if necessary to fill the buffer.
     * This mimics OpenSSL's BN_bn2bin and BN_bn2lebin behavior, which treat the number as unsigned.
     * @param buf the buffer to write into; must not be null
     * @param bufsize the number of bytes to write; must be non-negative and no greater than buf.length
     * @param littleEndian whether to write the bytes in little-endian order (true) or big-endian order (false)
     * @throws IllegalArgumentException if bufsize is invalid or too small to hold the number
     */
    public void getBytes(byte[] buf, int bufsize, boolean littleEndian) {
        if (buf == null) throw new NullPointerException("buf");
        if (bufsize < 0 || bufsize > buf.length) throw new IllegalArgumentException("Invalid bufsize");

        byte[] magBE = toUnsignedBigEndianMagnitude(this.bn); // unsigned magnitude

        if (magBE.length > bufsize) {
            throw new IllegalArgumentException(
                    "Buffer of size " + bufsize + " is too small to hold bignum with " + magBE.length + " bytes."
            );
        }

        Arrays.fill(buf, 0, bufsize, (byte) 0);

        if (!littleEndian) {
            // left pad
            int start = bufsize - magBE.length;
            System.arraycopy(magBE, 0, buf, start, magBE.length);
        } else {
            // little-endian: least significant byte first; pad zeros at the "end"
            // equivalent to: reverse(magBE) at the start of the buffer
            byte[] magLE = reverseCopy(magBE);
            System.arraycopy(magLE, 0, buf, 0, magLE.length);
        }
    }

    /**
     * Returns a byte array containing the unsigned big-endian (or little-endian) representation of this BigNumber,
     * padded with leading zeros if necessary to reach at least minSize bytes.
     * This mimics OpenSSL's BN_bn2bin and BN_bn2lebin behavior, which treat the number as unsigned.
     * @param minSize the minimum size of the output byte array; if the number requires more bytes, it will be larger
     * @param littleEndian whether to return the bytes in little-endian order (true) or big-endian order (false)
     * @return a byte array containing the unsigned big-endian (or little-endian) representation of this BigNumber
     */
    public byte[] toByteVector(int minSize, boolean littleEndian) {
        int len = Math.max(getNumBytes(), Math.max(0, minSize));
        byte[] out = new byte[len];
        getBytes(out, len, littleEndian);
        return out;
    }

    /**
     * Returns the hexadecimal string representation of this BigNumber. This mimics OpenSSL's BN_bn2hex behavior.
     * Note: OpenSSL's BN_bn2hex returns uppercase without "0x" prefix, and no leading zeros.
     * @return the hexadecimal string representation of this BigNumber
     */
    public String asHexStr() {
        // OpenSSL BN_bn2hex returns uppercase without "0x"
        return this.bn.abs().toString(16).toUpperCase();
    }

    /**
     * Returns the decimal string representation of this BigNumber. This mimics OpenSSL's BN_bn2dec behavior.
     * Note: OpenSSL's BN_bn2dec returns a string with a leading '-' for negative numbers, and no leading zeros for positive numbers.
     * @return the decimal string representation of this BigNumber
     */
    public String asDecStr() {
        return this.bn.toString(10);
    }

    /**
     * Utility method to reverse a byte array. This is used for converting between big-endian and little-endian formats.
     * @param in the input byte array
     * @return a new byte array with the bytes in reverse order
     */
    private static byte[] reverseCopy(byte[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0, j = in.length - 1; i < in.length; i++, j--) {
            out[i] = in[j];
        }
        return out;
    }

    /**
     * Converts a BigInteger to an unsigned big-endian byte array representing its magnitude.
     * This mimics the behavior of OpenSSL's BN_bn2bin and BN_bn2lebin, which treat the number as unsigned.
     * @param x the BigInteger to convert
     * @return a byte array containing the unsigned big-endian magnitude of x
     */
    private static byte[] toUnsignedBigEndianMagnitude(BigInteger x) {
        if (x.signum() == 0) return new byte[0];
        byte[] twos = x.abs().toByteArray();
        if (twos.length > 1 && twos[0] == 0x00) {
            return Arrays.copyOfRange(twos, 1, twos.length);
        }
        return twos;
    }
}
