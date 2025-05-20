package com.netdatel.documentserviceapi.service.impl;

import com.netdatel.documentserviceapi.exception.PermissionDeniedException;
import com.netdatel.documentserviceapi.exception.ResourceNotFoundException;
import com.netdatel.documentserviceapi.model.dto.request.BatchDownloadRequest;
import com.netdatel.documentserviceapi.model.entity.BatchDownload;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.Folder;
import com.netdatel.documentserviceapi.model.enums.BatchStatus;
import com.netdatel.documentserviceapi.model.enums.FileStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatusColor;
import com.netdatel.documentserviceapi.repository.BatchDownloadRepository;
import com.netdatel.documentserviceapi.repository.FileRepository;
import com.netdatel.documentserviceapi.repository.FolderRepository;
import com.netdatel.documentserviceapi.service.BatchDownloadService;
import com.netdatel.documentserviceapi.service.FileService;
import com.netdatel.documentserviceapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchDownloadServiceImpl implements BatchDownloadService {

    private final BatchDownloadRepository batchDownloadRepository;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final FileService fileService;

    @Async
    @Override
    public BatchDownload startBatchDownload(BatchDownloadRequest request, Integer userId) {
        log.info("Starting batch download for folder: {} by user: {}", request.getFolderId(), userId);

        Folder folder = folderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));

        // Contar archivos a procesar
        int totalFiles = countFilesToDownload(folder, request.isIncludeSubfolders());

        // Crear registro de descarga masiva
        BatchDownload batchDownload = BatchDownload.builder()
                .folder(folder)
                .userId(userId)
                .status(BatchStatus.PENDING)
                .totalFiles(totalFiles)
                .processedFiles(0)
                .includeSubfolders(request.isIncludeSubfolders())
                .expirationTime(LocalDateTime.now().plusHours(24))
                .build();

        BatchDownload savedBatchDownload = batchDownloadRepository.save(batchDownload);

        // Procesar en segundo plano
        processDownloadAsync(savedBatchDownload);

        return savedBatchDownload;
    }

    @Override
    public BatchDownload getBatchDownload(Integer id, Integer userId) {
        BatchDownload batchDownload = batchDownloadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Descarga masiva no encontrada"));

        // Verificar que pertenezca al usuario
        if (!batchDownload.getUserId().equals(userId)) {
            throw new PermissionDeniedException("No tienes permiso para acceder a esta descarga");
        }

        return batchDownload;
    }

    @Override
    public List<BatchDownload> getUserBatchDownloads(Integer userId) {
        return batchDownloadRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Async
    protected void processDownloadAsync(BatchDownload batchDownload) {
        try {
            log.info("Processing batch download: {}", batchDownload.getId());

            // Actualizar estado
            batchDownload.setStatus(BatchStatus.PROCESSING);
            batchDownloadRepository.save(batchDownload);

            // Crear archivo ZIP temporal
            Path tempFile = Files.createTempFile("batch-download-", ".zip");

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempFile.toFile()))) {
                // Agregar archivos al ZIP
                int processedFiles = addFilesToZip(batchDownload.getFolder(), "", zipOut,
                        batchDownload.isIncludeSubfolders());

                batchDownload.setProcessedFiles(processedFiles);
            }

            // Subir archivo ZIP a MinIO
            String objectKey = "batch-downloads/" + UUID.randomUUID() + "/download.zip";
            try (InputStream inputStream = new FileInputStream(tempFile.toFile())) {
                storageService.uploadFile(
                        inputStream,
                        Files.size(tempFile),
                        objectKey,
                        "application/zip",
                        Collections.singletonMap("Content-Disposition", "attachment; filename=\"download.zip\"")
                );
            }

            // Generar URL prefirmada
            String downloadUrl = storageService.generatePresignedUrl(objectKey, 24 * 60); // 24 horas

            // Actualizar registro de descarga
            batchDownload.setStatus(BatchStatus.COMPLETED);
            batchDownload.setDownloadUrl(downloadUrl);
            batchDownload.setCompletedAt(LocalDateTime.now());
            batchDownload.setFileSize(Files.size(tempFile));

            // Actualizar estado de archivos a DOWNLOADED
            updateFilesViewStatus(batchDownload.getFolder(), batchDownload.isIncludeSubfolders());
            Files.delete(tempFile);

        } catch (Exception e) {
            log.error("Error processing batch download", e);
            batchDownload.setStatus(BatchStatus.FAILED);
            batchDownload.setErrorMessage(e.getMessage());
        }

        batchDownloadRepository.save(batchDownload);
    }

    private int countFilesToDownload(Folder folder, boolean includeSubfolders) {
        int count = fileRepository.countByFolderIdAndStatus(folder.getId(), FileStatus.ACTIVE);

        if (includeSubfolders) {
            List<Folder> subfolders = folderRepository.findByParentId(folder.getId());
            for (Folder subfolder : subfolders) {
                count += countFilesToDownload(subfolder, true);
            }
        }

        return count;
    }

    private int addFilesToZip(Folder folder, String pathPrefix, ZipOutputStream zipOut, boolean includeSubfolders) {
        int processedFiles = 0;

        // Agregar archivos de la carpeta actual
        List<File> files = fileRepository.findByFolderIdAndStatus(folder.getId(), FileStatus.ACTIVE);
        for (File file : files) {
            try {
                String entryPath = pathPrefix + file.getName();
                ZipEntry zipEntry = new ZipEntry(entryPath);
                zipOut.putNextEntry(zipEntry);

                byte[] fileContent = storageService.downloadFile(file.getStorageKey());
                zipOut.write(fileContent, 0, fileContent.length);
                zipOut.closeEntry();

                processedFiles++;
            } catch (Exception e) {
                log.error("Error adding file to ZIP: {}", file.getId(), e);
                // Continuar con el siguiente archivo
            }
        }

        // Procesar subcarpetas si es necesario
        if (includeSubfolders) {
            List<Folder> subfolders = folderRepository.findByParentId(folder.getId());
            for (Folder subfolder : subfolders) {
                String newPathPrefix = pathPrefix + subfolder.getName() + "/";
                processedFiles += addFilesToZip(subfolder, newPathPrefix, zipOut, true);
            }
        }

        return processedFiles;
    }

    private void updateFilesViewStatus(Folder folder, boolean includeSubfolders) {
        // Actualizar archivos de la carpeta actual
        List<File> files = fileRepository.findByFolderIdAndStatus(folder.getId(), FileStatus.ACTIVE);
        for (File file : files) {
            if (file.getViewStatus() != ViewStatus.DOWNLOADED) {
                fileService.updateFileViewStatus(file.getId(), ViewStatus.NOT_DOWNLOADED, ViewStatusColor.RED);
            }
        }

        // Procesar subcarpetas si es necesario
        if (includeSubfolders) {
            List<Folder> subfolders = folderRepository.findByParentId(folder.getId());
            for (Folder subfolder : subfolders) {
                updateFilesViewStatus(subfolder, true);
            }
        }
    }
}