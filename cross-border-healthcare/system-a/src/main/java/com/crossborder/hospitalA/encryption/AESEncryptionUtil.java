package com.crossborder.hospitalA.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncryptionUtil {
    private static final String AES = "AES";
    private static final String SECRET_KEY = "1234567890123456"; // 16-char key

    // Encrypt and return Base64 encoded string (existing method)
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Encrypt and return raw bytes (new method for binary Kafka messages)
    public static byte[] encryptToBytes(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data.getBytes());
    }
}
