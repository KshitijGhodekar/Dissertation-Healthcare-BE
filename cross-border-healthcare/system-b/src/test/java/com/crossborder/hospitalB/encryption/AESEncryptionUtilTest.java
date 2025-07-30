package com.crossborder.hospitalB.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class AESEncryptionUtilTest {

    private static final String AES_KEY = "1234567890123456";
    private static final String SAMPLE_TEXT = "ConfidentialPatientInfo";

    @BeforeEach
    public void setup() {
        AESEncryptionUtil.setKey(AES_KEY);
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        String encryptedBase64 = AESEncryptionUtil.encrypt(SAMPLE_TEXT);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        String decrypted = AESEncryptionUtil.decrypt(encryptedBytes);

        assertEquals(SAMPLE_TEXT, decrypted);
    }

    @Test
    public void testEncryptToBytes() throws Exception {
        byte[] encryptedBytes = AESEncryptionUtil.encryptToBytes(SAMPLE_TEXT);
        assertNotNull(encryptedBytes);
        assertTrue(encryptedBytes.length > 16); // should include IV + cipherText
        String decrypted = AESEncryptionUtil.decrypt(encryptedBytes);
        assertEquals(SAMPLE_TEXT, decrypted);
    }
}
