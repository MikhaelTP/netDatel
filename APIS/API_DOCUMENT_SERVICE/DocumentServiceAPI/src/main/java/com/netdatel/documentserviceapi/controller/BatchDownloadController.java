package com.netdatel.documentserviceapi.controller;

import com.netdatel.documentserviceapi.exception.PermissionDeniedException;
import com.netdatel.documentserviceapi.model.dto.request.BatchDownloadRequest;
import com.netdatel.documentserviceapi.model.dto.response.ApiResponse;
import com.netdatel.documentserviceapi.model.dto.response.BatchDownloadResponse;
import com.netdatel.documentserviceapi.model.entity.BatchDownload;
import com.netdatel.documentserviceapi.security.CurrentUserId;
import com.netdatel.documentserviceapi.service.BatchDownloadService;
import com.netdatel.documentserviceapi.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch-downloads")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Batch Downloads", description = "API para gestionar descargas masivas de carpetas")
public class BatchDownloadController {
    private final BatchDownloadService batchDownloadService;
    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Iniciar descarga masiva", description = "Inicia un proceso de descarga masiva de una carpeta")
    public ResponseEntity<ApiResponse<BatchDownloadResponse>> startBatchDownload(
            @Valid @RequestBody BatchDownloadRequest request,
            @CurrentUserId Integer userId) {

        // Verificar permiso de descarga en la carpeta
        if (!permissionService.userCanDownloadFolder(userId, request.getFolderId()))
            throw new PermissionDeniedException("No tienes permiso para descargar archivos de esta carpeta");

        BatchDownload batchDownload = batchDownloadService.startBatchDownload(request, userId);

        return ResponseEntity.accepted()
                .body(ApiResponse.success("Proceso de descarga masiva iniciado", mapToResponse(batchDownload)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de descarga", description = "Obtiene el estado actual de un proceso de descarga masiva")
    public ResponseEntity<ApiResponse<BatchDownloadResponse>> getBatchDownloadStatus(
            @PathVariable Integer id,
            @CurrentUserId Integer userId) {

        BatchDownload batchDownload = batchDownloadService.getBatchDownload(id, userId);

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(batchDownload)));
    }

    @GetMapping("/user")
    @Operation(summary = "Listar descargas", description = "Lista todas las descargas masivas del usuario actual")
    public ResponseEntity<ApiResponse<List<BatchDownloadResponse>>> getUserBatchDownloads(
            @CurrentUserId Integer userId) {

        List<BatchDownload> batchDownloads = batchDownloadService.getUserBatchDownloads(userId);

        return ResponseEntity.ok(ApiResponse.success(
                batchDownloads.stream().map(this::mapToResponse).collect(Collectors.toList())));
    }

    private BatchDownloadResponse mapToResponse(BatchDownload batchDownload) {
        return BatchDownloadResponse.builder()
                .id(batchDownload.getId())
                .folderId(batchDownload.getFolder().getId())
                .status(batchDownload.getStatus())
                .totalFiles(batchDownload.getTotalFiles())
                .processedFiles(batchDownload.getProcessedFiles())
                .downloadUrl(batchDownload.getDownloadUrl())
                .createdAt(batchDownload.getCreatedAt())
                .completedAt(batchDownload.getCompletedAt())
                .expirationTime(batchDownload.getExpirationTime())
                .includeSubfolders(batchDownload.isIncludeSubfolders())
                .errorMessage(batchDownload.getErrorMessage())
                .build();
    }
}
