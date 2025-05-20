package com.netdatel.documentserviceapi.model.dto.response;

import com.netdatel.documentserviceapi.model.enums.FileStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatusColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {
    private Integer id;
    private Integer folderId;
    private String name;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private FileStatus status;
    private ViewStatus viewStatus;
    private ViewStatusColor viewStatusColor;
    private LocalDateTime uploadDate;
    private LocalDateTime lastViewedDate;
    private LocalDateTime lastDownloadedDate;
    private Integer uploadedBy;
    private Integer version;
    private Map<String, Object> metadata;

    // Métodos de conveniencia para el cliente

    // Obtener la extensión del archivo
    public String getExtension() {
        if (name == null || name.isEmpty() || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
    }

    // Obtener un formato legible del tamaño del archivo
    public String getFormattedSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    // Determinar si el archivo es una imagen
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    // Determinar si el archivo es un documento
    public boolean isDocument() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals("application/pdf") ||
                mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                mimeType.equals("text/plain");
    }

    // Determinar si el archivo es una hoja de cálculo
    public boolean isSpreadsheet() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                mimeType.equals("text/csv");
    }

    // Determinar si el archivo es una presentación
    public boolean isPresentation() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals("application/vnd.ms-powerpoint") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation");
    }

    // Obtener un ícono adecuado según el tipo de archivo (util para interfaces de usuario)
    public String getIconClass() {
        if (isImage()) {
            return "fa-file-image";
        } else if (isDocument()) {
            return "fa-file-alt";
        } else if (isSpreadsheet()) {
            return "fa-file-excel";
        } else if (isPresentation()) {
            return "fa-file-powerpoint";
        } else if (mimeType != null && mimeType.equals("application/zip")) {
            return "fa-file-archive";
        } else if (mimeType != null && mimeType.startsWith("video/")) {
            return "fa-file-video";
        } else if (mimeType != null && mimeType.startsWith("audio/")) {
            return "fa-file-audio";
        } else {
            return "fa-file";
        }
    }

    // Obtener una descripción legible del tiempo transcurrido desde la carga
    public String getTimeAgo() {
        if (uploadDate == null) {
            return "Desconocido";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(uploadDate, now);

        if (minutes < 1) {
            return "Justo ahora";
        } else if (minutes < 60) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "") + " atrás";
        } else {
            long hours = ChronoUnit.HOURS.between(uploadDate, now);
            if (hours < 24) {
                return hours + " hora" + (hours > 1 ? "s" : "") + " atrás";
            } else {
                long days = ChronoUnit.DAYS.between(uploadDate, now);
                if (days < 30) {
                    return days + " día" + (days > 1 ? "s" : "") + " atrás";
                } else {
                    long months = ChronoUnit.MONTHS.between(uploadDate, now);
                    if (months < 12) {
                        return months + " mes" + (months > 1 ? "es" : "") + " atrás";
                    } else {
                        long years = ChronoUnit.YEARS.between(uploadDate, now);
                        return years + " año" + (years > 1 ? "s" : "") + " atrás";
                    }
                }
            }
        }
    }
}