package com.stoonproduction.jobapplicatio.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Auth DissoJak 2022
 * Utility class for securely hashing and verifying passwords using BCrypt.
 * BCrypt automatically handles salt generation and storage.
 */
public class PasswordHasher {

    /**
     * Hashes a plaintext password.
     * @param plainPassword The raw password to hash.
     * @return The hashed password (includes salt).
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plaintext password against a stored hash.
     * @param plainPassword The raw password to check.
     * @param hashedPassword The stored hashed password.
     * @return True if the password matches, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() ||
                hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}