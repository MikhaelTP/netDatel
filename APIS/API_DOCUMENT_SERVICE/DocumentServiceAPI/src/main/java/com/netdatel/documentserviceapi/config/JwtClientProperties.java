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
    private String publicKeyPath;  // Cambiado de publicKey a publicKeyPath
    private Key publicKey; // Añadido para almacenar la clave ya procesada

    @PostConstruct
    public void init() throws Exception {
        // Cargar la clave pública desde el archivo
        if (publicKeyPath != null && publicKeyPath.startsWith("classpath:")) {
            String path = publicKeyPath.substring("classpath:".length());
            Resource resource = new ClassPathResource(path);
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                char[] keyChars = new char[(int) resource.contentLength()];
                reader.read(keyChars);
                String publicKeyPEM = new String(keyChars);
                this.publicKey = convertStringToPublicKey(publicKeyPEM);
            }
        }
    }

    private Key convertStringToPublicKey(String publicKeyPEM) throws Exception {
        // Eliminar encabezados y pies PEM si existen
        String publicKeyContent = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public Key getPublicKey() throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Public key is not configured");
        }
        return publicKey;
    }

    // Getter y setter para publicKeyPath
    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }
}