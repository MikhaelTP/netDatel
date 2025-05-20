package com.netdatel.documentserviceapi.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@ConfigurationProperties(prefix = "jwt")
public class JwtClientProperties {
    private String publicKey;

    @PostConstruct
    public void init() throws Exception {
        // Si la clave pública se proporciona como un recurso classpath, cargarla
        if (publicKey != null && publicKey.startsWith("classpath:")) {
            String path = publicKey.substring("classpath:".length());
            Resource resource = new ClassPathResource(path);
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                char[] keyChars = new char[(int) resource.contentLength()];
                reader.read(keyChars);
                publicKey = new String(keyChars);
            }
        }
    }

    public Key getPublicKey() throws Exception {
        if (publicKey == null || publicKey.isEmpty()) {
            throw new IllegalStateException("Public key is not configured");
        }

        // Eliminar encabezados y pies PEM si existen
        String publicKeyContent = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
