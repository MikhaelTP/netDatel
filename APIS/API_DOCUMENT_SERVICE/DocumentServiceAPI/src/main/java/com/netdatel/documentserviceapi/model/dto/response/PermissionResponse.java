package com.netdatel.documentserviceapi.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private Integer id;
    private Integer resourceId;
    private String resourceType; // "FOLDER" o "FILE"
    private Integer userId;
    private boolean canRead;
    private boolean canWrite;
    private boolean canDelete;
    private boolean canDownload;
    private LocalDateTime grantedAt;
    private Integer grantedBy;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean isActive;
}