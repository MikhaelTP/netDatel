package com.netdatel.documentserviceapi.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class FileTypeValidator {
    private static final Map<String, List<String>> ALLOWED_EXTENSIONS_BY_MODULE = Map.of(
            "1", Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "jpg", "jpeg", "png", "gif"),
            "2", Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "jpg", "jpeg", "png", "gif"),
            "3", Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "csv", "jpg", "jpeg", "png")
    );

    private static final Map<String, List<String>> ALLOWED_MIME_TYPES_BY_MODULE = Map.of(
            "1", Arrays.asList(
                    "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "text/plain", "text/csv", "image/jpeg", "image/png", "image/gif"
            ),
            "2", Arrays.asList(
                    "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "text/plain", "text/csv", "image/jpeg", "image/png", "image/gif"
            ),
            "3", Arrays.asList(
                    "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "text/csv", "image/jpeg", "image/png"
            )
    );

    /**
     * Valida si la extensión del archivo está permitida para el módulo
     */
    public boolean isExtensionAllowed(String filename, String moduleId) {
        if (filename == null || moduleId == null) {
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensions = ALLOWED_EXTENSIONS_BY_MODULE.get(moduleId);

        return allowedExtensions != null && allowedExtensions.contains(extension);
    }

    /**
     * Valida si el tipo MIME está permitido para el módulo
     */
    public boolean isMimeTypeAllowed(String mimeType, String moduleId) {
        if (mimeType == null || moduleId == null) {
            return false;
        }

        List<String> allowedMimeTypes = ALLOWED_MIME_TYPES_BY_MODULE.get(moduleId);
        return allowedMimeTypes != null && allowedMimeTypes.contains(mimeType.toLowerCase());
    }

    /**
     * Valida si el archivo está permitido basado en su extensión y tipo MIME
     */
    public boolean isFileAllowed(String filename, String mimeType, String moduleId) {
        return isExtensionAllowed(filename, moduleId) && isMimeTypeAllowed(mimeType, moduleId);
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}