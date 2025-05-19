package com.netdatel.identityserviceapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.identityserviceapi.domain.dto.AuditLogDto;
import com.netdatel.identityserviceapi.domain.entity.AuditLog;
import com.netdatel.identityserviceapi.domain.entity.User;
import com.netdatel.identityserviceapi.domain.mapper.AuditLogMapper;
import com.netdatel.identityserviceapi.repository.AuditLogRepository;
import com.netdatel.identityserviceapi.repository.UserRepository;
import com.netdatel.identityserviceapi.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void logEvent(String action, String entityType, String entityId, Object oldValues, Object newValues) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);

            // Get current authenticated user if available
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                Optional<User> currentUser = userRepository.findByUsername(authentication.getName());
                currentUser.ifPresent(auditLog::setUser);
            }

            // Get client IP if available
            try {
                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = attr.getRequest();
                String ip = getClientIp(request);
                auditLog.setIpAddress(ip);
            } catch (Exception e) {
                log.debug("Could not get client IP", e);
            }

            // Serialize old and new values
            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }

            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Error serializing audit log values", e);
        } catch (Exception e) {
            log.error("Error saving audit log", e);
        }
    }

    @Override
    public Page<AuditLogDto> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogDto> getAuditLogsByUser(Integer userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return auditLogRepository.findByUser(user, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogDto> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogDto> getAuditLogsByEntityTypeAndId(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogDto> getAuditLogsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(from, to, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogDto> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(auditLogMapper::toDto);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
