package com.broker.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Secret {
    private static final Integer SALT_LENGTH = 12;


    private static final String appid = "356ac7f17da1f2ed";
    private static final String secretKey = "3DEFAF2271DD5C75F2B559AB9D3296761268DF24E47120EEC6737EB2";


    private static String byteToHexString(byte[] b) {
        StringBuilder hexString = new StringBuilder();

        for (byte value : b) {
            String hex = Integer.toHexString(value & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            hexString.append(hex.toUpperCase());
        }

        return hexString.toString();
    }

    public static String sign() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(salt);
            md.update((appid + secretKey).getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            byte[] pwd = new byte[digest.length + SALT_LENGTH];
            System.arraycopy(salt, 0, pwd, 0, SALT_LENGTH);
            System.arraycopy(digest, 0, pwd, SALT_LENGTH, digest.length);
            return byteToHexString(pwd);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}