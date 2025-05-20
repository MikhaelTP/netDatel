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
public class ClientSpaceResponse {
    private Integer id;
    private Integer clientId;
    private Integer moduleId;
    private String storagePath;
    private Long totalQuotaBytes;
    private Long usedBytes;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double usedPercentage;

    // Método para calcular el porcentaje de uso
    public Double getUsedPercentage() {
        if (totalQuotaBytes <= 0) {
            return 0.0;
        }
        return (usedBytes * 100.0) / totalQuotaBytes;
    }

    // Método para obtener una representación legible del espacio total
    public String getFormattedTotalQuota() {
        return formatSize(totalQuotaBytes);
    }

    // Método para obtener una representación legible del espacio usado
    public String getFormattedUsedSpace() {
        return formatSize(usedBytes);
    }

    // Método de utilidad para formatear el tamaño en bytes a una forma legible
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}