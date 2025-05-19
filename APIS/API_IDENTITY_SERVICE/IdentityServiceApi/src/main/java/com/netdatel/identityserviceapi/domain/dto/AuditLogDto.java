package com.netdatel.identityserviceapi.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogDto {

    private Integer id;
    private Long userId;
    private String username;
    private String action;
    private String entityType;
    private String entityId;
    private Object oldValues;
    private Object newValues;
    private String ipAddress;
    private LocalDateTime timestamp;

    // Campos adicionales para mostrar información de contexto
    private String userFullName;
    private String userType;
}