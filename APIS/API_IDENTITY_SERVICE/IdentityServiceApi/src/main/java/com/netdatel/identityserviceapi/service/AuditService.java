package com.netdatel.identityserviceapi.service;

import com.netdatel.identityserviceapi.domain.dto.AuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditService {

    void logEvent(String action, String entityType, String entityId, Object oldValues, Object newValues);

    Page<AuditLogDto> getAuditLogs(Pageable pageable);

    Page<AuditLogDto> getAuditLogsByUser(Integer userId, Pageable pageable);

    Page<AuditLogDto> getAuditLogsByEntityType(String entityType, Pageable pageable);

    Page<AuditLogDto> getAuditLogsByEntityTypeAndId(String entityType, String entityId, Pageable pageable);

    Page<AuditLogDto> getAuditLogsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLogDto> getAuditLogsByAction(String action, Pageable pageable);
}
