package com.crossborder.hospitalA.encryption;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AESEncryptionUtilTest {

    @BeforeAll
    public static void initKey() {
        // This must match the key in application.yml
        AESEncryptionUtil.setKey("1234567890123456");
    }

    @Test
    public void testEncryptionAndDecryption() throws Exception {
        String original = "This is a test message";

        String encrypted = AESEncryptionUtil.encrypt(original);
        assertNotNull(encrypted);

        String decrypted = AESEncryptionUtil.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testRawByteDecryption() throws Exception {
        String original = "Sensitive Data";
        String encrypted = AESEncryptionUtil.encrypt(original);
        byte[] encryptedBytes = java.util.Base64.getDecoder().decode(encrypted);

        String decrypted = AESEncryptionUtil.decrypt(encryptedBytes);
        assertEquals(original, decrypted);
    }
}
