package com.crossborder.hospitalB.encryption;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class ECDSAUtil {
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

    // Verifies ECDSA signature using local public key
    public static boolean verify(String data, String base64Signature) throws Exception {
        PublicKey publicKey = loadPublicKey();
        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(publicKey);
        verifier.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = Base64.getDecoder().decode(base64Signature);
        return verifier.verify(sigBytes);
    }

    // Debug: Export public key
    public static String getPublicKeyBase64() throws Exception {
        PublicKey publicKey = loadPublicKey();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // New method for unit test signing (uses local test private key)
    public static String sign(String message) throws Exception {
        InputStream is = ECDSAUtil.class.getClassLoader().getResourceAsStream("ecdsa/ec_private_pkcs8.pem");
        if (is == null) throw new IllegalArgumentException("Private key file not found");

        byte[] keyBytes = is.readAllBytes();
        String pem = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey privateKey = kf.generatePrivate(spec);

        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signer.sign();

        return Base64.getEncoder().encodeToString(signature);
    }
}
