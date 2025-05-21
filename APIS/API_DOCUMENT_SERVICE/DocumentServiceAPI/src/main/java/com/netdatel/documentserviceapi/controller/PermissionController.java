package com.netdatel.documentserviceapi.controller;

import com.netdatel.documentserviceapi.model.dto.request.PermissionRequest;
import com.netdatel.documentserviceapi.model.dto.response.ApiResponse;
import com.netdatel.documentserviceapi.model.dto.response.PermissionResponse;
import com.netdatel.documentserviceapi.model.entity.FilePermission;
import com.netdatel.documentserviceapi.model.entity.FolderPermission;
import com.netdatel.documentserviceapi.security.CurrentUserId;
import com.netdatel.documentserviceapi.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "API para gestionar permisos de carpetas y archivos")
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping("/folder/{folderId}")
    @Operation(summary = "Obtener permisos de carpeta", description = "Obtiene todos los permisos asignados a una carpeta")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getFolderPermissions(@PathVariable Integer folderId) {
        List<FolderPermission> permissions = permissionService.getFolderPermissions(folderId);
        return ResponseEntity.ok(ApiResponse.success(
                permissions.stream().map(this::mapToFolderPermissionResponse).collect(Collectors.toList())));
    }

    @PostMapping("/folder/{folderId}")
    @Operation(summary = "Asignar permisos a carpeta", description = "Asigna permisos a un usuario para una carpeta")
    public ResponseEntity<ApiResponse<PermissionResponse>> assignFolderPermission(
            @PathVariable Integer folderId,
            @Valid @RequestBody PermissionRequest request,
            @CurrentUserId Integer userId) {

        FolderPermission permission = permissionService.assignFolderPermission(folderId, request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permisos asignados exitosamente", mapToFolderPermissionResponse(permission)));
    }

    @PutMapping("/folder/{id}")
    @Operation(summary = "Actualizar permisos de carpeta", description = "Actualiza los permisos asignados a una carpeta")
    public ResponseEntity<ApiResponse<PermissionResponse>> updateFolderPermission(
            @PathVariable Integer id,
            @Valid @RequestBody PermissionRequest request,
            @CurrentUserId Integer userId) {

        FolderPermission permission = permissionService.updateFolderPermission(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success("Permisos actualizados exitosamente", mapToFolderPermissionResponse(permission)));
    }

    @DeleteMapping("/folder/{id}")
    @Operation(summary = "Revocar permisos de carpeta", description = "Revoca los permisos asignados a una carpeta")
    public ResponseEntity<ApiResponse<Void>> revokeFolderPermission(@PathVariable Integer id) {
        permissionService.revokeFolderPermission(id);

        return ResponseEntity.ok(ApiResponse.success("Permisos revocados exitosamente", null));
    }

    @GetMapping("/file/{fileId}")
    @Operation(summary = "Obtener permisos de archivo", description = "Obtiene todos los permisos asignados a un archivo")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getFilePermissions(@PathVariable Integer fileId) {
        List<FilePermission> permissions = permissionService.getFilePermissions(fileId);
        return ResponseEntity.ok(ApiResponse.success(
                permissions.stream().map(this::mapToFilePermissionResponse).collect(Collectors.toList())));
    }

    @PostMapping("/file/{fileId}")
    @Operation(summary = "Asignar permisos a archivo", description = "Asigna permisos a un usuario para un archivo")
    public ResponseEntity<ApiResponse<PermissionResponse>> assignFilePermission(
            @PathVariable Integer fileId,
            @Valid @RequestBody PermissionRequest request,
            @CurrentUserId Integer userId) {

        FilePermission permission = permissionService.assignFilePermission(fileId, request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permisos asignados exitosamente", mapToFilePermissionResponse(permission)));
    }

    @PutMapping("/file/{id}")
    @Operation(summary = "Actualizar permisos de archivo", description = "Actualiza los permisos asignados a un archivo")
    public ResponseEntity<ApiResponse<PermissionResponse>> updateFilePermission(
            @PathVariable Integer id,
            @Valid @RequestBody PermissionRequest request,
            @CurrentUserId Integer userId) {

        FilePermission permission = permissionService.updateFilePermission(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success("Permisos actualizados exitosamente", mapToFilePermissionResponse(permission)));
    }

    @DeleteMapping("/file/{id}")
    @Operation(summary = "Revocar permisos de archivo", description = "Revoca los permisos asignados a un archivo")
    public ResponseEntity<ApiResponse<Void>> revokeFilePermission(@PathVariable Integer id) {
        permissionService.revokeFilePermission(id);

        return ResponseEntity.ok(ApiResponse.success("Permisos revocados exitosamente", null));
    }

    @GetMapping("/check/folder/{folderId}/user/{userId}")
    @Operation(summary = "Verificar permisos en carpeta", description = "Verifica los permisos de un usuario en una carpeta")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFolderPermissions(
            @PathVariable Integer folderId,
            @PathVariable Integer userId) {

        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("canRead", permissionService.userCanReadFolder(userId, folderId));
        permissions.put("canWrite", permissionService.userCanWriteFolder(userId, folderId));
        permissions.put("canDelete", permissionService.userCanDeleteFolder(userId, folderId));
        permissions.put("canDownload", permissionService.userCanDownloadFolder(userId, folderId));

        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/check/file/{fileId}/user/{userId}")
    @Operation(summary = "Verificar permisos en archivo", description = "Verifica los permisos de un usuario en un archivo")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFilePermissions(
            @PathVariable Integer fileId,
            @PathVariable Integer userId) {

        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("canRead", permissionService.userCanReadFile(userId, fileId));
        permissions.put("canWrite", permissionService.userCanWriteFile(userId, fileId));
        permissions.put("canDelete", permissionService.userCanDeleteFile(userId, fileId));
        permissions.put("canDownload", permissionService.userCanDownloadFile(userId, fileId));

        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    private PermissionResponse mapToFolderPermissionResponse(FolderPermission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .resourceId(permission.getFolder().getId())
                .resourceType("FOLDER")
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
}