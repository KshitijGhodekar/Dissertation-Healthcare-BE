package com.crossborder.hospitalA.encryption;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class ECDSAUtil {

    // Load private key from PEM (PKCS#8 format)
    public static PrivateKey loadPrivateKey() throws Exception {
        InputStream is = ECDSAUtil.class.getClassLoader().getResourceAsStream("ecdsa/ec_private_pkcs8.pem");
        if (is == null) throw new IllegalArgumentException("Private key file not found");

        byte[] keyBytes = is.readAllBytes();
        String pem = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(keySpec);
    }

    // Load public key from PEM (X.509 format)
    public static PublicKey loadPublicKey() throws Exception {
        InputStream is = ECDSAUtil.class.getClassLoader().getResourceAsStream("ecdsa/ec_public.pem");
        if (is == null) throw new IllegalArgumentException("Public key file not found");

        byte[] keyBytes = is.readAllBytes();
        String pem = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
    }

    // Sign data using private key
    public static String sign(String data) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signer.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    // Optional: Export public key in Base64 string for viewing/logging
    public static String getPublicKeyBase64() throws Exception {
        PublicKey publicKey = loadPublicKey();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
