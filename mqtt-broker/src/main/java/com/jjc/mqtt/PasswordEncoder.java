package com.jjc.mqtt;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordEncoder {

    private static final Logger log = LoggerFactory.getLogger(PasswordEncoder.class);

    private static final String BCRYPT_PREFIX = "$2a$";
    private static final String BCRYPT_PREFIX_2B = "$2b$";

    public static boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        if (isBCryptHash(storedPassword)) {
            return BCrypt.verifyer().verify(rawPassword.toCharArray(), storedPassword).verified;
        }
        // Plaintext fallback with warning
        log.warn("WARNING: Password is stored in plaintext. Please use BCrypt-encoded passwords for security.");
        return storedPassword.equals(rawPassword);
    }

    public static String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    }

    public static boolean isBCryptHash(String password) {
        return password != null && (password.startsWith(BCRYPT_PREFIX) || password.startsWith(BCRYPT_PREFIX_2B));
    }
}
