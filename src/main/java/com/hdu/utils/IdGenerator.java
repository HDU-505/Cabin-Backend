package com.hdu.utils;


import java.security.SecureRandom;

public class IdGenerator {
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generate20CharId() {
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

}
