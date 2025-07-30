package com.crossborder.hospitalA.config;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AESKeyInitializerTest {

    @Test
    void testSetKeyDoesNotThrow() {
        assertDoesNotThrow(() -> AESEncryptionUtil.setKey("testkey123456789"));
    }

    @Test
    void testSetKeyWithInvalidKeyThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                AESEncryptionUtil.setKey("short")
        );
        assertEquals("AES key must be 16 characters long.", exception.getMessage());
    }
}
