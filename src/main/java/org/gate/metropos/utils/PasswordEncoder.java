package org.gate.metropos.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordEncoder {
    private final BCrypt.Hasher hasher = BCrypt.withDefaults();
    private final BCrypt.Verifyer verifyer = BCrypt.verifyer();

    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return verifyer.verify(rawPassword.toCharArray(), encodedPassword).verified;
    }

}
