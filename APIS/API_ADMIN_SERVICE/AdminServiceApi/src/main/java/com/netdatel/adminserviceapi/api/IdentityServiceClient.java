package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.dto.response.UserCredentialsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class IdentityServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${identity.service.url:https://localhost:8080}")
    private String identityServiceUrl;

    public UserCredentialsResponse getUserCredentials(String email) {
        try {
            String url = identityServiceUrl + "/api/users/credentials?email=" + email;
            return restTemplate.getForObject(url, UserCredentialsResponse.class);
        } catch (Exception e) {
            log.error("Error obteniendo credenciales para {}: {}", email, e.getMessage());
            throw new RuntimeException("No se pudieron obtener las credenciales temporales");
        }
    }
}