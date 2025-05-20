package com.netdatel.documentserviceapi.utils;

import org.springframework.stereotype.Component;

@Component
public class PathUtil {

    /**
     * Normaliza una ruta eliminando barras duplicadas y garantizando que empiece con barra
     */
    public String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        // Reemplazar múltiples barras por una sola
        String normalized = path.replaceAll("/+", "/");

        // Asegurar que comience con /
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        // Eliminar la barra final si no es la raíz
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    /**
     * Combina segmentos de ruta en una ruta normalizada
     */
    public String combinePath(String... segments) {
        StringBuilder path = new StringBuilder();

        for (String segment : segments) {
            if (segment != null && !segment.isEmpty()) {
                if (path.length() > 0 && !path.toString().endsWith("/") && !segment.startsWith("/")) {
                    path.append("/");
                }
                path.append(segment);
            }
        }

        return normalizePath(path.toString());
    }

    /**
     * Extrae el nombre de la ruta
     */
    public String getName(String path) {
        String normalized = normalizePath(path);

        if ("/".equals(normalized)) {
            return "";
        }

        int lastSlashIndex = normalized.lastIndexOf('/');
        return normalized.substring(lastSlashIndex + 1);
    }

    /**
     * Obtiene la ruta del padre
     */
    public String getParentPath(String path) {
        String normalized = normalizePath(path);

        if ("/".equals(normalized)) {
            return null; // La raíz no tiene padre
        }

        int lastSlashIndex = normalized.lastIndexOf('/');
        if (lastSlashIndex == 0) {
            return "/"; // El padre es la raíz
        }

        return normalized.substring(0, lastSlashIndex);
    }
}

