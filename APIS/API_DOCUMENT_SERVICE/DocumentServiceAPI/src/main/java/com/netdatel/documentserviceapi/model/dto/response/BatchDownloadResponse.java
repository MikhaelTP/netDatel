package com.netdatel.documentserviceapi.model.dto.response;

import com.netdatel.documentserviceapi.model.enums.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDownloadResponse {
    private Integer id;
    private Integer folderId;
    private BatchStatus status;
    private Integer totalFiles;
    private Integer processedFiles;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime expirationTime;
    private boolean includeSubfolders;
    private String errorMessage;
}