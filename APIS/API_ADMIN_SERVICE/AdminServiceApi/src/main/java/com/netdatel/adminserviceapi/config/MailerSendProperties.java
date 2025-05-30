package com.netdatel.adminserviceapi.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración centralizada para MailerSend
 */
@Configuration
@ConfigurationProperties(prefix = "app.mailersend")
@Data
@Validated
@Slf4j
public class MailerSendProperties {

    @NotEmpty(message = "MailerSend API token es obligatorio")
    private String apiToken;

    @NotEmpty(message = "Email remitente es obligatorio")
    @Email(message = "Email remitente debe ser válido")
    private String fromEmail;

    @NotEmpty(message = "Nombre remitente es obligatorio")
    private String fromName;

    private Retry retry = new Retry();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Retry {
        private int maxAttempts = 3;
        private int delaySeconds = 60;
    }

    @Data
    public static class RateLimit {
        private int requestsPerMinute = 120;
    }

    @PostConstruct
    public void validateConfiguration() {
        log.info("🔧 Configuración MailerSend inicializada:");
        log.info("  - From Email: {}", fromEmail);
        log.info("  - From Name: {}", fromName);
        log.info("  - API Token presente: {}", apiToken != null && !apiToken.isEmpty());
        log.info("  - Max reintentos: {}", retry.maxAttempts);
        log.info("  - Rate limit: {} req/min", rateLimit.requestsPerMinute);

        // Validar que el token no sea el valor por defecto
        if (apiToken != null && apiToken.startsWith("ms-token-xxxxxxxx")) {
            log.warn("⚠️  ADVERTENCIA: Usando token MailerSend por defecto. Configura MAILERSEND_API_TOKEN");
        }

        // Validar que el email no sea el valor por defecto
        if (fromEmail != null && fromEmail.equals("noreply@yourdomain.com")) {
            log.warn("⚠️  ADVERTENCIA: Usando email por defecto. Configura MAILERSEND_FROM_EMAIL");
        }
    }
}