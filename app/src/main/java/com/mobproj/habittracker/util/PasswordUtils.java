package com.mobproj.habittracker.util;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing using salted PBKDF2.
 *
 * <p>Hashes are stored as {@code iterations:base64(salt):base64(hash)} so the
 * salt and work factor travel with each record and can be raised over time
 * without a schema change.
 *
 * <p>{@code PBKDF2WithHmacSHA1} is used because it is available on every
 * Android API level (the SHA-256 variant of PBKDF2 requires API 26, and this
 * app supports minSdk 24). PBKDF2-HMAC-SHA1 remains a standard, unbroken KDF;
 * the known weaknesses of raw SHA-1 do not apply to its use inside HMAC/PBKDF2.
 */
public final class PasswordUtils {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 100_000;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {}

    /**
     * Hashes the given password with a fresh 16-byte random salt.
     *
     * @return a record of the form {@code iterations:base64(salt):base64(hash)}
     */
    public static String hash(String plain) {
        if (plain == null) plain = "";
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(plain.toCharArray(), salt, ITERATIONS);
        return ITERATIONS + ":"
                + Base64.encodeToString(salt, Base64.NO_WRAP) + ":"
                + Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    /**
     * Verifies a plaintext password against a stored
     * {@code iterations:base64(salt):base64(hash)} record.
     *
     * @return true if the password matches, false for a mismatch or a
     *         malformed/legacy stored value
     */
    public static boolean verify(String plain, String stored) {
        if (stored == null) return false;
        if (plain == null) plain = "";
        String[] parts = stored.split(":");
        if (parts.length != 3) return false;
        try {
            int iterations = Integer.parseInt(parts[0]);
            if (iterations <= 0) return false;
            byte[] salt = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] expected = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] actual = pbkdf2(plain.toCharArray(), salt, iterations);
            return constantTimeEquals(expected, actual);
        } catch (NumberFormatException | IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
