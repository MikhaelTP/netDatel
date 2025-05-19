package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.request.NotificationRequest;
import com.netdatel.adminserviceapi.dto.response.NotificationResponse;
import com.netdatel.adminserviceapi.entity.enums.NotificationStatus;
import com.netdatel.adminserviceapi.entity.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    /**
     * Envía una notificación
     * @param request Datos de la notificación
     * @param userId ID del usuario que realiza la operación
     * @return Datos de la notificación enviada
     */
    NotificationResponse sendNotification(NotificationRequest request, Integer userId);

    /**
     * Obtiene una notificación por su ID
     * @param id ID de la notificación
     * @return Datos de la notificación
     */
    NotificationResponse getNotificationById(Integer id);

    /**
     * Obtiene notificaciones paginadas con filtros opcionales
     * @param clientId ID del cliente (opcional)
     * @param targetType Tipo de destinatario (opcional)
     * @param status Estado de la notificación (opcional)
     * @param pageable Configuración de paginación
     * @return Lista paginada de notificaciones
     */
    Page<NotificationResponse> getNotifications(
            Integer clientId, TargetType targetType, NotificationStatus status, Pageable pageable);

    /**
     * Reintenta el envío de una notificación fallida
     * @param id ID de la notificación
     * @param userId ID del usuario que realiza la operación
     * @return Datos de la notificación reintentada
     */
    NotificationResponse retryNotification(Integer id, Integer userId);

    /**
     * Reintenta automáticamente notificaciones fallidas
     * @return Número de notificaciones reintentadas
     */
    int retryFailedNotifications();
}
