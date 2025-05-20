package com.netdatel.documentserviceapi.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderResponse {
    private Integer id;
    private Integer clientSpaceId;
    private String name;
    private String description;
    private Integer parentId;
    private String path;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private Map<String, Object> attributes;

    // Campos adicionales para la UI
    private Long itemCount;
    private Long folderCount;
    private Long fileCount;
    private Long totalSize;
    private List<FolderResponse> subfolders;

    // Métodos de utilidad

    /**
     * Obtiene el nombre de la carpeta principal de la ruta
     */
    public String getParentName() {
        if (path == null || path.equals("/") || path.equals("/" + name)) {
            return "Raíz";
        }

        String parentPath = path.substring(0, path.lastIndexOf('/'));
        if (parentPath.isEmpty() || parentPath.equals("/")) {
            return "Raíz";
        }

        return parentPath.substring(parentPath.lastIndexOf('/') + 1);
    }

    /**
     * Determina si esta carpeta es una carpeta raíz
     */
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * Verifica si esta carpeta tiene subcarpetas
     */
    public boolean hasSubfolders() {
        return subfolders != null && !subfolders.isEmpty();
    }

    /**
     * Obtiene la profundidad de la carpeta en la jerarquía
     */
    public int getDepth() {
        if (path == null || path.equals("/")) {
            return 0;
        }

        // Contar las barras '/' en la ruta, esto nos da la profundidad
        return (int) path.chars().filter(ch -> ch == '/').count() - (path.endsWith("/") ? 1 : 0);
    }

    /**
     * Obtiene una representación legible del tamaño total
     */
    public String getFormattedTotalSize() {
        if (totalSize == null) {
            return "Desconocido";
        }

        if (totalSize < 1024) {
            return totalSize + " B";
        } else if (totalSize < 1024 * 1024) {
            return String.format("%.2f KB", totalSize / 1024.0);
        } else if (totalSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", totalSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", totalSize / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Obtiene un ícono apropiado para la carpeta basado en su contenido
     */
    public String getIconClass() {
        if (totalSize == null || totalSize == 0) {
            return "fa-folder-o"; // Carpeta vacía
        } else if (fileCount != null && fileCount > 0 &&
                (folderCount == null || folderCount == 0)) {
            return "fa-folder-open"; // Carpeta con archivos pero sin subcarpetas
        } else if (folderCount != null && folderCount > 0) {
            return "fa-folder-plus"; // Carpeta con subcarpetas
        } else {
            return "fa-folder"; // Carpeta genérica
        }
    }

    /**
     * Obtiene la ruta relativa de esta carpeta desde la raíz
     */
    public List<PathSegment> getBreadcrumbs() {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return Collections.singletonList(new PathSegment("Raíz", "/"));
        }

        List<PathSegment> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new PathSegment("Raíz", "/"));

        String[] segments = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            currentPath.append("/").append(segment);
            breadcrumbs.add(new PathSegment(segment, currentPath.toString()));
        }

        return breadcrumbs;
    }

    /**
     * Clase interna para representar segmentos de la ruta en las migas de pan
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathSegment {
        private String name;
        private String path;
    }

    /**
     * Obtiene el tiempo transcurrido desde la creación de la carpeta
     */
    public String getTimeAgo() {
        if (createdAt == null) {
            return "Desconocido";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);

        if (minutes < 1) {
            return "Justo ahora";
        } else if (minutes < 60) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "") + " atrás";
        } else {
            long hours = ChronoUnit.HOURS.between(createdAt, now);
            if (hours < 24) {
                return hours + " hora" + (hours > 1 ? "s" : "") + " atrás";
            } else {
                long days = ChronoUnit.DAYS.between(createdAt, now);
                if (days < 30) {
                    return days + " día" + (days > 1 ? "s" : "") + " atrás";
                } else {
                    long months = ChronoUnit.MONTHS.between(createdAt, now);
                    if (months < 12) {
                        return months + " mes" + (months > 1 ? "es" : "") + " atrás";
                    } else {
                        long years = ChronoUnit.YEARS.between(createdAt, now);
                        return years + " año" + (years > 1 ? "s" : "") + " atrás";
                    }
                }
            }
        }
    }
}