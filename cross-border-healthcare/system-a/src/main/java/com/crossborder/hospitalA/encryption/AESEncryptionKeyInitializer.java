package com.crossborder.hospitalA.config;

import com.crossborder.hospitalA.encryption.AESEncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AESEncryptionKeyInitializer {

    @Value("${security.aes-key}")
    private String aesKey;

    @PostConstruct
    public void init() {
        AESEncryptionUtil.setKey(aesKey);
    }
}
