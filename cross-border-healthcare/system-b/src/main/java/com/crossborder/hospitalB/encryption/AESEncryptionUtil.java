package com.crossborder.hospitalB.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESEncryptionUtil {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static String aesKey;

    public static void setKey(String key) {
        if (key == null || key.length() != 16) {
            throw new IllegalArgumentException("AES key must be 16 characters long.");
        }
        aesKey = key;
    }

    private static byte[] getSecretKeyBytes() {
        if (aesKey == null || aesKey.length() != 16) {
            throw new IllegalStateException("AES key is not initialized.");
        }
        return aesKey.getBytes();
    }

    public static String encrypt(String data) throws Exception {
        byte[] keyBytes = getSecretKeyBytes();
        byte[] iv = generateIV();
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] ciphertext = cipher.doFinal(data.getBytes());

        byte[] ivAndCiphertext = new byte[IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, ivAndCiphertext, 0, IV_LENGTH);
        System.arraycopy(ciphertext, 0, ivAndCiphertext, IV_LENGTH, ciphertext.length);

        return Base64.getEncoder().encodeToString(ivAndCiphertext);
    }

    public static byte[] encryptToBytes(String data) throws Exception {
        return Base64.getDecoder().decode(encrypt(data));
    }

    public static String decrypt(String base64Data) throws Exception {
        byte[] ivAndCiphertext = Base64.getDecoder().decode(base64Data);
        return decrypt(ivAndCiphertext);
    }

    public static String decrypt(byte[] ivAndCiphertext) throws Exception {
        byte[] keyBytes = getSecretKeyBytes();
        byte[] iv = new byte[IV_LENGTH];
        byte[] ciphertext = new byte[ivAndCiphertext.length - IV_LENGTH];
        System.arraycopy(ivAndCiphertext, 0, iv, 0, IV_LENGTH);
        System.arraycopy(ivAndCiphertext, IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext);
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
