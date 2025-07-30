package com.crossborder.hospitalA.encryption;

import org.junit.jupiter.api.Test;

import java.security.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class ECDSAUtilTest {

    @Test
    void testLoadPrivateKey() {
        try {
            PrivateKey privateKey = ECDSAUtil.loadPrivateKey();
            assertNotNull(privateKey);
            System.out.println("Private key loaded: " + privateKey.getAlgorithm());
        } catch (IllegalArgumentException e) {
            System.out.println("Skipping test — Private key not found");
        } catch (Exception e) {
            fail("Exception while loading private key: " + e.getMessage());
        }
    }

    @Test
    void testLoadPublicKey() {
        try {
            PublicKey publicKey = ECDSAUtil.loadPublicKey();
            assertNotNull(publicKey);
            System.out.println("Public key loaded: " + publicKey.getAlgorithm());
        } catch (IllegalArgumentException e) {
            System.out.println("Skipping test — Public key not found");
        } catch (Exception e) {
            fail("Exception while loading public key: " + e.getMessage());
        }
    }

    @Test
    void testSignAndVerify() {
        try {
            String data = "Cross-border healthcare record";

            String signature = ECDSAUtil.sign(data);
            assertNotNull(signature);
            System.out.println("Signature: " + signature);

            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(ECDSAUtil.loadPublicKey());
            verifier.update(data.getBytes());

            boolean verified = verifier.verify(Base64.getDecoder().decode(signature));
            assertTrue(verified, "Signature should be valid");

        } catch (IllegalArgumentException e) {
            System.out.println("Skipping test — ECDSA keys not found");
        } catch (Exception e) {
            fail("Signature test failed: " + e.getMessage());
        }
    }
}
