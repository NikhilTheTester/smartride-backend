package com.smartride.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Password hashing with BCrypt (salted, slow hash designed for passwords).
 * Only the spring-security-crypto library is used; the full Spring Security framework is not enabled.
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** Creates a new BCrypt hash (different every time because of the random salt). */
    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** Checks a typed password against a stored BCrypt hash. */
    public static boolean matches(String rawPassword, String storedHash) {
        return storedHash != null && ENCODER.matches(rawPassword, storedHash);
    }
}
