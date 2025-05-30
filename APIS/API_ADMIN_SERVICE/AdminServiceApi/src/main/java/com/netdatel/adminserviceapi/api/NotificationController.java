package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.dto.request.NotificationRequest;
import com.netdatel.adminserviceapi.dto.response.NotificationResponse;
import com.netdatel.adminserviceapi.entity.enums.NotificationStatus;
import com.netdatel.adminserviceapi.entity.enums.TargetType;
import com.netdatel.adminserviceapi.security.CurrentUserId;
import com.netdatel.adminserviceapi.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin:notification:create')")
    public NotificationResponse createNotification(
            @RequestBody @Valid NotificationRequest request,
            @CurrentUserId Integer userId) {
        return notificationService.sendNotification(request, userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:notification:read')")
    public Page<NotificationResponse> getNotifications(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) TargetType targetType,
            @RequestParam(required = false) NotificationStatus status,
            Pageable pageable) {
        return notificationService.getNotifications(clientId, targetType, status, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:notification:read')")
    public NotificationResponse getNotification(@PathVariable Integer id) {
        return notificationService.getNotificationById(id);
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasAuthority('admin:notification:update')")
    public NotificationResponse retryNotification(
            @PathVariable Integer id,
            @CurrentUserId Integer userId) {
        return notificationService.retryNotification(id, userId);
    }

    @PostMapping("/retry-failed")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('admin:notification:update')")
    public int retryFailedNotifications() {
        return notificationService.retryFailedNotifications();
    }
}