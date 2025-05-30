package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.external.EmailRequest;
import com.netdatel.adminserviceapi.dto.external.EmailResponse;
import com.netdatel.adminserviceapi.dto.request.NotificationRequest;
import com.netdatel.adminserviceapi.dto.response.NotificationResponse;
import com.netdatel.adminserviceapi.entity.ClientAdministrator;
import com.netdatel.adminserviceapi.entity.Notification;
import com.netdatel.adminserviceapi.entity.WorkersRegistration;
import com.netdatel.adminserviceapi.entity.enums.NotificationStatus;
import com.netdatel.adminserviceapi.entity.enums.TargetType;
import com.netdatel.adminserviceapi.exception.ResourceNotFoundException;
import com.netdatel.adminserviceapi.exception.ValidationException;
import com.netdatel.adminserviceapi.mapper.NotificationMapper;
import com.netdatel.adminserviceapi.repository.ClientAdministratorRepository;
import com.netdatel.adminserviceapi.repository.ClientRepository;
import com.netdatel.adminserviceapi.repository.NotificationRepository;
import com.netdatel.adminserviceapi.repository.WorkersRegistrationRepository;
import com.netdatel.adminserviceapi.service.integration.MailerSendAdapter;
import com.netdatel.adminserviceapi.service.integration.MailerSendClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ClientRepository clientRepository;
    private final ClientAdministratorRepository clientAdministratorRepository;
    private final WorkersRegistrationRepository workersRegistrationRepository;
    private final NotificationMapper notificationMapper;
    private final MailerSendClient mailerSendClient;
    private final MailerSendAdapter mailerSendAdapter;

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request, Integer userId) {
        log.info("Creando notificación - ClientId: {}, TargetType: {}, Type: {}",
                request.getClientId(), request.getTargetType(), request.getNotificationType());

        // Validar cliente si se proporciona
        if (request.getClientId() != null) {
            if (!clientRepository.existsById(request.getClientId())) {
                throw new ResourceNotFoundException("Cliente", "id", request.getClientId());
            }
        }

        // Crear la notificación
        Notification notification = notificationMapper.toEntity(request);
        notification.setCreatedBy(userId);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setSendDate(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);

        log.info("Notificación creada con ID: {}, iniciando envío asíncrono", savedNotification.getId());

        // Enviar la notificación de forma asíncrona
        sendNotificationAsync(savedNotification);

        return notificationMapper.toDto(savedNotification);
    }

    @Override
    public NotificationResponse getNotificationById(Integer id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", id));

        return notificationMapper.toDto(notification);
    }

    @Override
    public Page<NotificationResponse> getNotifications(
            Integer clientId, TargetType targetType, NotificationStatus status, Pageable pageable) {

        Specification<Notification> spec = Specification.where(null);

        if (clientId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("clientId"), clientId));
        }

        if (targetType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("targetType"), targetType));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Notification> notifications = notificationRepository.findAll(spec, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional
    public NotificationResponse retryNotification(Integer id, Integer userId) {
        log.info("Reintentando notificación ID: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", id));

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new ValidationException("Solo notificaciones fallidas pueden ser reintentadas");
        }

        // Actualizar notificación
        notification.setStatus(NotificationStatus.PENDING);
        notification.setLastRetry(LocalDateTime.now());
        notification.setRetryCount(notification.getRetryCount() + 1);

        Notification updatedNotification = notificationRepository.save(notification);

        // Enviar notificación de forma asíncrona
        sendNotificationAsync(updatedNotification);

        return notificationMapper.toDto(updatedNotification);
    }

    @Override
    @Transactional
    public int retryFailedNotifications() {
        // Buscar notificaciones fallidas con menos de 3 reintentos y último reintento hace más de 1 hora
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<Notification> failedNotifications = notificationRepository
                .findFailedNotificationsForRetry(3, cutoffTime);

        if (failedNotifications.isEmpty()) {
            log.info("No hay notificaciones fallidas para reintentar");
            return 0;
        }

        log.info("Reintentando {} notificaciones fallidas", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setLastRetry(LocalDateTime.now());
            notification.setRetryCount(notification.getRetryCount() + 1);

            sendNotificationAsync(notification);
        }

        return failedNotifications.size();
    }

    /**
     * Envía una notificación de forma asíncrona
     */
    @Async
    protected void sendNotificationAsync(Notification notification) {
        log.info("Enviando notificación asíncrona ID: {} - Type: {}",
                notification.getId(), notification.getNotificationType());

        try {
            EmailRequest emailRequest = createEmailRequest(notification);

            if (emailRequest.getRecipients() == null || emailRequest.getRecipients().isEmpty()) {
                log.warn("No se encontraron destinatarios para notificación ID: {}", notification.getId());
                updateNotificationStatus(notification, NotificationStatus.FAILED, "No se encontraron destinatarios", null);
                return;
            }

            log.info("Enviando email a {} destinatarios via MailerSend", emailRequest.getRecipients().size());

            // ✅ USAR MAILERSEND ADAPTER
            EmailResponse response = mailerSendAdapter.sendEmail(emailRequest);

            if (response.getSuccess()) {
                log.info("Email enviado exitosamente - NotificationId: {}, MailerSend MessageId: {}",
                        notification.getId(), response.getMessageId());
                updateNotificationStatus(notification, NotificationStatus.SENT, null, response.getMessageId());
            } else {
                log.error("Error enviando email - NotificationId: {}, Error: {}",
                        notification.getId(), response.getError());
                updateNotificationStatus(notification, NotificationStatus.FAILED, response.getError(), null);
            }

        } catch (Exception e) {
            log.error("Excepción al enviar notificación ID {}: {}", notification.getId(), e.getMessage(), e);
            updateNotificationStatus(notification, NotificationStatus.FAILED,
                    "Error interno: " + e.getMessage(), null);
        }
    }


    /**
     * Actualiza el estado de la notificación en la base de datos
     */
    private void updateNotificationStatus(Notification notification, NotificationStatus status,
                                          String errorMessage, String messageId) {
        try {
            notification.setStatus(status);
            notification.setErrorMessage(errorMessage);

            // Guardar messageId de MailerSend para tracking futuro
            if (messageId != null) {
                String currentDetails = notification.getDetails();
                String newDetails = "MailerSend ID: " + messageId;
                notification.setDetails(currentDetails != null ? currentDetails + " | " + newDetails : newDetails);
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Error actualizando estado de notificación ID {}: {}",
                    notification.getId(), e.getMessage(), e);
        }
    }

    /**
     * Crea la solicitud de email para la notificación
     */
    private EmailRequest createEmailRequest(Notification notification) {
        log.debug("Creando EmailRequest para notificación ID: {}, TargetType: {}",
                notification.getId(), notification.getTargetType());

        EmailRequest request = new EmailRequest();

        // Configurar destinatarios según targetType - LÓGICA EXISTENTE
        if (notification.getTargetType() == TargetType.CLIENT) {
            // Obtener emails de los administradores del cliente
            List<String> adminEmails = clientAdministratorRepository
                    .findByClientIdAndStatusActive(notification.getClientId())
                    .stream()
                    .map(ClientAdministrator::getEmail)
                    .collect(Collectors.toList());

            log.debug("Encontrados {} administradores activos para cliente ID: {}",
                    adminEmails.size(), notification.getClientId());
            request.setRecipients(adminEmails);

        } else if (notification.getTargetType() == TargetType.ADMINISTRATOR) {
            // Obtener email del administrador específico
            ClientAdministrator admin = clientAdministratorRepository
                    .findById(notification.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Administrador", "id", notification.getTargetId()));

            request.setRecipients(Collections.singletonList(admin.getEmail()));

        } else if (notification.getTargetType() == TargetType.WORKERS) {
            // Obtener emails de los trabajadores pre-registrados
            List<String> workerEmails = workersRegistrationRepository
                    .findByClientId(notification.getClientId())
                    .stream()
                    .map(WorkersRegistration::getEmail)
                    .collect(Collectors.toList());

            log.debug("Encontrados {} trabajadores para cliente ID: {}",
                    workerEmails.size(), notification.getClientId());
            request.setRecipients(workerEmails);
        }

        request.setSubject(notification.getSubject());
        request.setHtmlContent(notification.getContent());

        return request;
    }
}
