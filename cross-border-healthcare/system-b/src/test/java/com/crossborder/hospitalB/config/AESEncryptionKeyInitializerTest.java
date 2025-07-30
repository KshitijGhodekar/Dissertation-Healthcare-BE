package com.crossborder.hospitalB.config;

import com.crossborder.hospitalB.encryption.AESEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class AESEncryptionKeyInitializerTest {

    @Test
    public void testInitSetsAESKeySuccessfully() throws Exception {
        // Arrange
        AESEncryptionKeyInitializer initializer = new AESEncryptionKeyInitializer();
        String testKey = "1234567890123456";

        // Simulate @Value injection
        ReflectionTestUtils.setField(initializer, "aesKey", testKey);

        // Act
        initializer.init();

        String original = "confidential";
        String encrypted = AESEncryptionUtil.encrypt(original);
        String decrypted = AESEncryptionUtil.decrypt(encrypted);

        assertEquals(original, decrypted);
    }
}
