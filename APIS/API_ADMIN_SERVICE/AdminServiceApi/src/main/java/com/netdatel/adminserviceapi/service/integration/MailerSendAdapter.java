package com.netdatel.adminserviceapi.service.integration;

import com.netdatel.adminserviceapi.dto.external.EmailRequest;
import com.netdatel.adminserviceapi.dto.external.EmailResponse;
import com.netdatel.adminserviceapi.dto.request.MailerSendRequest;
import com.netdatel.adminserviceapi.dto.response.MailerSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Adaptador para mantener compatibilidad con tu interfaz existente
@Service
@RequiredArgsConstructor
@Slf4j
public class MailerSendAdapter {

    private final MailerSendClient mailerSendClient;

    @Value("${app.mailersend.from-email}")
    private String fromEmail;

    @Value("${app.mailersend.from-name}")
    private String fromName;

    /**
     * Adapta tu EmailRequest existente a MailerSend y mantiene la interfaz compatible
     */
    public EmailResponse sendEmail(EmailRequest emailRequest) {
        try {
            log.info("Enviando email via MailerSend - Recipients: {}, Subject: {}",
                    emailRequest.getRecipients().size(), emailRequest.getSubject());

            // Convertir EmailRequest a MailerSendRequest
            MailerSendRequest mailerSendRequest = MailerSendRequest.builder()
                    .from(MailerSendRequest.EmailAddress.builder()
                            .email(fromEmail)
                            .name(fromName)
                            .build())
                    .to(emailRequest.getRecipients().stream()
                            .map(email -> MailerSendRequest.EmailAddress.builder()
                                    .email(email)
                                    .build())
                            .collect(Collectors.toList()))
                    .subject(emailRequest.getSubject())
                    .html(emailRequest.getHtmlContent())
                    .tags(List.of("admin-service", "notification"))
                    .build();

            // Enviar via MailerSend
            MailerSendResponse response = mailerSendClient.sendEmail(mailerSendRequest);

            log.info("MailerSend response - Success: {}, MessageId: {}",
                    response.isSuccess(), response.getMessageId());

            // Convertir respuesta a formato compatible
            return EmailResponse.fromMailerSend(response);

        } catch (Exception e) {
            log.error("Error sending email via MailerSend: {}", e.getMessage(), e);

            return EmailResponse.builder()
                    .success(false)
                    .error("Error enviando email: " + e.getMessage())
                    .build();
        }
    }
}