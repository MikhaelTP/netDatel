package com.netdatel.identityserviceapi.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.identityserviceapi.domain.dto.AuditLogDto;
import com.netdatel.identityserviceapi.domain.entity.AuditLog;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class AuditLogMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userFullName", expression = "java(getFullName(auditLog))")
    @Mapping(target = "userType", expression = "java(getUserType(auditLog))")
    @Mapping(target = "oldValues", source = "oldValues")
    @Mapping(target = "newValues", source = "newValues")
    public abstract AuditLogDto toDto(AuditLog auditLog);

    @Mapping(target = "user", ignore = true)
    public abstract AuditLog toEntity(AuditLogDto auditLogDto);

    // Helper methods
    protected String getFullName(AuditLog auditLog) {
        if (auditLog.getUser() == null) {
            return null;
        }
        String firstName = auditLog.getUser().getFirstName() != null ? auditLog.getUser().getFirstName() : "";
        String lastName = auditLog.getUser().getLastName() != null ? auditLog.getUser().getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    protected String getUserType(AuditLog auditLog) {
        if (auditLog.getUser() == null || auditLog.getUser().getUserType() == null) {
            return null;
        }
        return auditLog.getUser().getUserType().name();
    }

    protected Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json; // Return as is if can't parse
        }
    }

    String map(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof String) {
                return (String) value;
            }
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value.toString();
        }
    }

    @AfterMapping
    protected void fillNulls(@MappingTarget AuditLogDto auditLogDto) {
        // Additional processing after mapping if needed
    }
}
