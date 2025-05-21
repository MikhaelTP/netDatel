package com.netdatel.documentserviceapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.documentserviceapi.model.dto.request.FolderRequest;
import com.netdatel.documentserviceapi.model.dto.request.PermissionRequest;
import com.netdatel.documentserviceapi.model.dto.response.ApiResponse;
import com.netdatel.documentserviceapi.model.dto.response.FolderResponse;
import com.netdatel.documentserviceapi.model.dto.response.PermissionResponse;
import com.netdatel.documentserviceapi.model.entity.Folder;
import com.netdatel.documentserviceapi.model.entity.FolderPermission;
import com.netdatel.documentserviceapi.security.CurrentUserId;
import com.netdatel.documentserviceapi.service.FolderService;
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

// src/main/java/com/netdatel/document/controller/FolderController.java
@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "Folders", description = "API para gestionar carpetas")
public class FolderController {
    private final FolderService folderService;
    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Crear carpeta", description = "Crea una nueva carpeta")
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @Valid @RequestBody FolderRequest request,
            @CurrentUserId Integer userId) {
        Folder folder = folderService.createFolder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Carpeta creada exitosamente", mapToResponse(folder)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener carpeta", description = "Obtiene una carpeta por su ID")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolder(@PathVariable Integer id) {
        Folder folder = folderService.getFolder(id);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(folder)));
    }

    @GetMapping("/client-space/{clientSpaceId}")
    @Operation(summary = "Obtener carpetas raíz", description = "Obtiene todas las carpetas raíz de un espacio de cliente")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getRootFolders(@PathVariable Integer clientSpaceId) {
        List<Folder> folders = folderService.getRootFolders(clientSpaceId);
        return ResponseEntity.ok(ApiResponse.success(
                folders.stream().map(this::mapToResponse).collect(Collectors.toList())));
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "Obtener subcarpetas", description = "Obtiene todas las subcarpetas de una carpeta padre")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getSubfolders(@PathVariable Integer parentId) {
        List<Folder> folders = folderService.getSubfolders(parentId);
        return ResponseEntity.ok(ApiResponse.success(
                folders.stream().map(this::mapToResponse).collect(Collectors.toList())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar carpeta", description = "Actualiza una carpeta existente")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable Integer id,
            @Valid @RequestBody FolderRequest request,
            @CurrentUserId Integer userId) {
        Folder folder = folderService.updateFolder(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Carpeta actualizada exitosamente", mapToResponse(folder)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar carpeta", description = "Elimina una carpeta y su contenido")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(@PathVariable Integer id) {
        folderService.deleteFolder(id);
        return ResponseEntity.ok(ApiResponse.success("Carpeta eliminada exitosamente", null));
    }

    @PostMapping("/{folderId}/permissions")
    @Operation(summary = "Asignar permisos", description = "Asigna permisos a un usuario para una carpeta")
    public ResponseEntity<ApiResponse<PermissionResponse>> assignPermissions(
            @PathVariable Integer folderId,
            @Valid @RequestBody PermissionRequest request,
            @CurrentUserId Integer userId) {
        FolderPermission permission = permissionService.assignFolderPermission(folderId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permisos asignados exitosamente", mapToPermissionResponse(permission)));
    }

    @GetMapping("/{folderId}/permissions")
    @Operation(summary = "Obtener permisos", description = "Obtiene todos los permisos asignados a una carpeta")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getFolderPermissions(@PathVariable Integer folderId) {
        List<FolderPermission> permissions = permissionService.getFolderPermissions(folderId);
        return ResponseEntity.ok(ApiResponse.success(
                permissions.stream().map(this::mapToPermissionResponse).collect(Collectors.toList())));
    }

    private FolderResponse mapToResponse(Folder folder) {
        return FolderResponse.builder()
                .id(folder.getId())
                .clientSpaceId(folder.getClientSpace().getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .path(folder.getPath())
                .isActive(folder.isActive())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .attributes(parseAttributes(folder.getAttributes()))
                .build();
    }

    private PermissionResponse mapToPermissionResponse(FolderPermission permission) {
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

    private Map<String, Object> parseAttributes(String attributesJson) {
        try {
            if (attributesJson == null || attributesJson.isEmpty() || "{}".equals(attributesJson)) {
                return new HashMap<>();
            }
            return new ObjectMapper().readValue(attributesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}