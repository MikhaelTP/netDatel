package com.netdatel.documentserviceapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.documentserviceapi.exception.ResourceNotFoundException;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.FileAccessHistory;
import com.netdatel.documentserviceapi.model.enums.ActionType;
import com.netdatel.documentserviceapi.repository.FileAccessHistoryRepository;
import com.netdatel.documentserviceapi.repository.FileRepository;
import com.netdatel.documentserviceapi.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final FileAccessHistoryRepository fileAccessHistoryRepository;
    private final FileRepository fileRepository;

    @Override
    public void logFileAccess(Integer fileId, Integer userId, ActionType actionType) {
        logFileAccess(fileId, userId, actionType, null, null, null);
    }

    @Override
    public void logFileAccess(Integer fileId, Integer userId, ActionType actionType, String ipAddress, String deviceInfo) {
        logFileAccess(fileId, userId, actionType, ipAddress, deviceInfo, null);
    }

    @Override
    public void logFileAccess(Integer fileId, Integer userId, ActionType actionType,
                              String ipAddress, String deviceInfo, Map<String, Object> additionalInfo) {
        try {
            log.debug("Logging file access: {}, user: {}, action: {}", fileId, userId, actionType);

            // Verificar que el archivo existe
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

            // Convertir additionalInfo a JSON si existe
            String additionalInfoJson = null;
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                try {
                    additionalInfoJson = new ObjectMapper().writeValueAsString(additionalInfo);
                } catch (Exception e) {
                    log.warn("Error converting additionalInfo to JSON", e);
                }
            }

            // Crear y guardar el registro de acceso
            FileAccessHistory accessHistory = FileAccessHistory.builder()
                    .file(file)
                    .userId(userId)
                    .actionType(actionType)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .additionalInfo(additionalInfoJson)
                    .build();

            fileAccessHistoryRepository.save(accessHistory);
        } catch (Exception e) {
            log.error("Error logging file access", e);
            // No lanzar excepción para evitar interrumpir la operación principal
        }
    }

    @Override
    public List<FileAccessHistory> getFileAccessHistory(Integer fileId) {
        log.info("Getting access history for file: {}", fileId);

        if (!fileRepository.existsById(fileId)) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }

        return fileAccessHistoryRepository.findByFileIdOrderByActionDateDesc(fileId);
    }

    @Override
    public List<FileAccessHistory> getUserAccessHistory(Integer userId) {
        log.info("Getting access history for user: {}", userId);

        return fileAccessHistoryRepository.findByUserIdOrderByActionDateDesc(userId);
    }

    @Override
    public List<FileAccessHistory> getUserAccessHistoryByDateRange(Integer userId,
                                                                   LocalDateTime startDate,
                                                                   LocalDateTime endDate) {
        log.info("Getting access history for user: {} between {} and {}", userId, startDate, endDate);

        return fileAccessHistoryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
}