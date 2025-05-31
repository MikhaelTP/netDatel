package com.netdatel.adminserviceapi.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.adminserviceapi.dto.external.EmailRequest;
import com.netdatel.adminserviceapi.dto.external.EmailResponse;
import com.netdatel.adminserviceapi.dto.request.MailerSendRequest;
import com.netdatel.adminserviceapi.dto.response.MailerSendResponse;
import com.netdatel.adminserviceapi.exception.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@FeignClient(
        name = "mailersend-api",
        url = "${app.services.mailer-send-url}",
        configuration = MailerSendConfig.class
)
public interface MailerSendClient {

    @PostMapping(value = "email", consumes = MediaType.APPLICATION_JSON_VALUE)
    MailerSendResponse sendEmail(@RequestBody MailerSendRequest request);
}

// Configuración para MailerSend
@Configuration
@Slf4j
class MailerSendConfig {

    @Value("${app.mailersend.api-token:}")
    private String apiToken;

    @Bean
    public feign.RequestInterceptor mailerSendRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + apiToken);
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("X-Requested-With", "XMLHttpRequest");

            log.debug("Adding MailerSend headers - Token present: {}",
                    apiToken != null && !apiToken.isEmpty());
        };
    }

    @Bean
    public ErrorDecoder mailerSendErrorDecoder() {
        return new MailerSendErrorDecoder();
    }
}

// Decodificador de errores para MailerSend
@Slf4j
class MailerSendErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = "Error comunicándose con MailerSend";

        try {
            if (response.body() != null) {
                String body = feign.Util.toString(response.body().asReader());
                var errorResponse = objectMapper.readTree(body);

                if (errorResponse.has("message")) {
                    message = errorResponse.get("message").asText();
                } else if (errorResponse.has("errors")) {
                    message = "Errores de validación: " + errorResponse.get("errors").toString();
                }
            }
        } catch (Exception e) {
            log.error("Error parsing MailerSend error response", e);
        }

        log.error("MailerSend API Error - Status: {}, Message: {}", response.status(), message);

        return switch (response.status()) {
            case 401 -> new ExternalServiceException("MailerSend", "Token de API inválido o expirado");
            case 403 -> new ExternalServiceException("MailerSend", "Acceso denegado - Verifica permisos");
            case 422 -> new ExternalServiceException("MailerSend", "Datos inválidos: " + message);
            case 429 -> new ExternalServiceException("MailerSend", "Límite de rate excedido - Intenta más tarde");
            case 500 -> new ExternalServiceException("MailerSend", "Error interno del servidor MailerSend");
            default -> new ExternalServiceException("MailerSend", "Error HTTP " + response.status() + ": " + message);
        };
    }
}
