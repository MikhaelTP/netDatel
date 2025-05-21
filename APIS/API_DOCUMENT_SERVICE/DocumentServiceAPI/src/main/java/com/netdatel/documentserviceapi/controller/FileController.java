package com.netdatel.documentserviceapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.documentserviceapi.exception.PermissionDeniedException;
import com.netdatel.documentserviceapi.model.dto.request.FileUploadRequest;
import com.netdatel.documentserviceapi.model.dto.request.PermissionRequest;
import com.netdatel.documentserviceapi.model.dto.response.ApiResponse;
import com.netdatel.documentserviceapi.model.dto.response.FileResponse;
import com.netdatel.documentserviceapi.model.dto.response.FileVersionResponse;
import com.netdatel.documentserviceapi.model.dto.response.PermissionResponse;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.FilePermission;
import com.netdatel.documentserviceapi.model.entity.FileVersion;
import com.netdatel.documentserviceapi.model.enums.ActionType;
import com.netdatel.documentserviceapi.model.enums.FileStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatusColor;
import com.netdatel.documentserviceapi.security.CurrentUserId;
import com.netdatel.documentserviceapi.service.AuditService;
import com.netdatel.documentserviceapi.service.FileService;
import com.netdatel.documentserviceapi.service.PermissionService;
import com.netdatel.documentserviceapi.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Files", description = "API para gestionar archivos")
public class FileController {
    private final FileService fileService;
    private final StorageService storageService;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir archivo", description = "Sube un nuevo archivo a una carpeta")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderId") Integer folderId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "metadata", required = false) String metadataJson,
            @CurrentUserId Integer userId) throws IOException {

        Map<String, Object> metadata = parseJson(metadataJson);

        FileUploadRequest request = FileUploadRequest.builder()
                .folderId(folderId)
                .name(name != null ? name : file.getOriginalFilename())
                .metadata(metadata)
                .build();

        File savedFile = fileService.uploadFile(request, file.getInputStream(), file.getSize(),
                file.getContentType(), userId);

        auditService.logFileAccess(savedFile.getId(), userId, ActionType.CREATE);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Archivo subido exitosamente", mapToResponse(savedFile)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener archivo", description = "Obtiene los metadatos de un archivo por su ID")
    public ResponseEntity<ApiResponse<FileResponse>> getFile(@PathVariable Integer id, @CurrentUserId Integer userId) {
        File file = fileService.getFile(id);

        // Verificar permiso de lectura
        if (!permissionService.userCanReadFile(userId, id)) {
            throw new PermissionDeniedException("No tienes permiso para ver este archivo");
        }

        auditService.logFileAccess(id, userId, ActionType.VIEW);

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(file)));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar archivo", description = "Descarga el contenido de un archivo")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer id, @CurrentUserId Integer userId) {
        File file = fileService.getFile(id);

        // Verificar permiso de descarga
        if (!permissionService.userCanDownloadFile(userId, id)) {
            throw new PermissionDeniedException("No tienes permiso para descargar este archivo");
        }

        fileService.updateFileViewStatus(id, ViewStatus.DOWNLOADED, ViewStatusColor.GREEN);
        auditService.logFileAccess(id, userId, ActionType.DOWNLOAD);

        byte[] content = storageService.downloadFile(file.getStorageKey());
        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .contentLength(content.length)
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/presigned-url")
    @Operation(summary = "Obtener URL prefirmada", description = "Genera una URL prefirmada para descargar un archivo")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "30") int expiryMinutes,
            @CurrentUserId Integer userId) {

        File file = fileService.getFile(id);

        // Verificar permiso de descarga
        if (!permissionService.userCanDownloadFile(userId, id)) {
            throw new PermissionDeniedException("No tienes permiso para descargar este archivo");
        }

        auditService.logFileAccess(id, userId, ActionType.DOWNLOAD);

        String url = storageService.generatePresignedUrl(file.getStorageKey(), expiryMinutes);

        return ResponseEntity.ok(ApiResponse.success(url));
    }

    @GetMapping("/folder/{folderId}")
    @Operation(summary = "Listar archivos", description = "Lista todos los archivos en una carpeta")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesByFolder(
            @PathVariable Integer folderId,
            @RequestParam(defaultValue = "ACTIVE") FileStatus status,
            @CurrentUserId Integer userId) {

        // Verificar permiso de lectura en la carpeta
        if (!permissionService.userCanReadFolder(userId, folderId)) {
            throw new PermissionDeniedException("No tienes permiso para listar archivos en esta carpeta");
        }

        List<File> files = fileService.getFilesByFolder(folderId, status);

        return ResponseEntity.ok(ApiResponse.success(
                files.stream().map(this::mapToResponse).collect(Collectors.toList())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar archivo", description = "Actualiza los metadatos de un archivo")
    public ResponseEntity<ApiResponse<FileResponse>> updateFile(
            @PathVariable Integer id,
            @Valid @RequestBody FileUploadRequest request,
            @CurrentUserId Integer userId) {

        // Verificar permiso de escritura
        if (!permissionService.userCanWriteFile(userId, id)) {
            throw new PermissionDeniedException("No tienes permiso para modificar este archivo");
        }

        File file = fileService.updateFile(id, request, userId);
        auditService.logFileAccess(id, userId, ActionType.UPDATE);

        return ResponseEntity.ok(ApiResponse.success("Archivo actualizado exitosamente", mapToResponse(file)));
    }

    @PostMapping("/{id}/version")
    @Operation(summary = "Subir nueva versión", description = "Sube una nueva versión de un archivo existente")
    public ResponseEntity<ApiResponse<FileResponse>> uploadNewVersion(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false) String comment,
            @CurrentUserId Integer userId) throws IOException {

        // Verificar permiso de escritura
        if (!permissionService.userCanWriteFile(userId, id)) {
            throw new PermissionDeniedException("No tienes permiso para modificar este archivo");
        }

        File updatedFile = fileService.uploadNewVersion(id, file.getInputStream(), file.getSize(),
                file.getContentType(), comment, userId);

        auditService.logFileAccess(id, userId, ActionType.UPDATE);

        return ResponseEntity.ok(ApiResponse.success("Nueva versión subida exitosamente", mapToResponse(updatedFile)));
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Listar versiones", description = "Lista todas las versiones de un archivo")
    public ResponseEntity<ApiResponse<List<FileVersionResponse>>> getFileVersions(
            @PathVariable Integer id,
            @CurrentUserId Integer userId) {

        // Verificar permiso de lectura
        if (!permissionService.userCanReadFile(userId, id)) {
            throw new PermissionDeniedException("No tienes permiso para ver este archivo");
        }

        List<FileVersion> versions = fileService.getFileVersions(id);

        return ResponseEntity.ok(ApiResponse.success(
                versions.stream().map(this::mapToVersionResponse).collect(Collectors.toList())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar archivo", description = "Elimina un archivo (cambio de estado a DELETED)")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Integer id, @CurrentUserId Integer userId) {
        // Verificar permiso de eliminación
        if (!permissionService.userCanDeleteFile(userId, id)) {
            throw new PermissionDeniedException("No tienes permiso para eliminar este archivo");
        }

        fileService.deleteFile(id, userId);
        auditService.logFileAccess(id, userId, ActionType.DELETE);

        return ResponseEntity.ok(ApiResponse.success("Archivo eliminado exitosamente", null));
    }

    @PostMapping("/{fileId}/permissions")
    @Operation(summary = "Asignar permisos", description = "Asigna permisos a un usuario para un archivo")
    public ResponseEntity<ApiResponse<PermissionResponse>> assignPermissions(
            @PathVariable Integer fileId,
            @Valid @RequestBody PermissionRequest request,
            @CurrentUserId Integer userId) {

        FilePermission permission = permissionService.assignFilePermission(fileId, request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permisos asignados exitosamente", mapToFilePermissionResponse(permission)));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar archivos", description = "Busca archivos por nombre o contenido")
    public ResponseEntity<ApiResponse<Page<FileResponse>>> searchFiles(
            @RequestParam String query,
            @RequestParam(required = false) Integer clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUserId Integer userId) {

        Page<File> files = fileService.searchFiles(query, clientId, PageRequest.of(page, size));

        Page<FileResponse> response = files.map(this::mapToResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private FileResponse mapToResponse(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .folderId(file.getFolder().getId())
                .name(file.getName())
                .originalName(file.getOriginalName())
                .fileSize(file.getFileSize())
                .mimeType(file.getMimeType())
                .status(file.getStatus())
                .viewStatus(file.getViewStatus())
                .viewStatusColor(file.getViewStatusColor())
                .uploadDate(file.getUploadDate())
                .lastViewedDate(file.getLastViewedDate())
                .lastDownloadedDate(file.getLastDownloadedDate())
                .version(file.getVersion())
                .metadata(parseJson(file.getMetadata()))
                .build();
    }

    private FileVersionResponse mapToVersionResponse(FileVersion version) {
        return FileVersionResponse.builder()
                .id(version.getId())
                .fileId(version.getFile().getId())
                .versionNumber(version.getVersionNumber())
                .fileSize(version.getFileSize())
                .createdAt(version.getCreatedAt())
                .createdBy(version.getCreatedBy())
                .changeComments(version.getChangeComments())
                .build();
    }

    private PermissionResponse mapToFilePermissionResponse(FilePermission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .resourceId(permission.getFile().getId())
                .resourceType("FILE")
                .userId(permission.getUserId())
                .canRead(permission.isCanRead())
                .canWrite(permission.isCanWrite())
                .canDelete(permission.isCanDelete())
                .canDownload(permission.isCanDownload())
                .grantedAt(permission.getGrantedAt())
                .grantedBy(permission.getGrantedBy())
                .validFrom(permission.getValidFrom())
                .validUntil(permission.getValidUntil())
                .isActive(permission.isActive())
                .build();
    }

    private Map<String, Object> parseJson(String json) {
        try {
            if (json == null || json.isEmpty() || "{}".equals(json)) {
                return new HashMap<>();
            }
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}