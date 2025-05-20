package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.model.entity.FileAccessHistory;
import com.netdatel.documentserviceapi.model.enums.ActionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {
    void logFileAccess(Integer fileId, Integer userId, ActionType actionType);
    void logFileAccess(Integer fileId, Integer userId, ActionType actionType, String ipAddress, String deviceInfo);
    void logFileAccess(Integer fileId, Integer userId, ActionType actionType, String ipAddress, String deviceInfo, Map<String, Object> additionalInfo);

    List<FileAccessHistory> getFileAccessHistory(Integer fileId);
    List<FileAccessHistory> getUserAccessHistory(Integer userId);
    List<FileAccessHistory> getUserAccessHistoryByDateRange(Integer userId, LocalDateTime startDate, LocalDateTime endDate);
}