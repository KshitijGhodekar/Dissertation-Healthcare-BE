package com.crossborder.hospitalB.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncryptionUtil {
    private static final String AES = "AES";
    private static final String SECRET_KEY = "1234567890123456"; // 16-char key

    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // New method for decrypting raw byte array (used in Kafka consumer)
    public static String decrypt(byte[] encryptedBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(encryptedBytes);
        return new String(decrypted);
    }
}
