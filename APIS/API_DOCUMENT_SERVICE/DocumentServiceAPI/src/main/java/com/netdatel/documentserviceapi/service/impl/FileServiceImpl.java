package com.netdatel.documentserviceapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.documentserviceapi.exception.InvalidRequestException;
import com.netdatel.documentserviceapi.exception.ResourceNotFoundException;
import com.netdatel.documentserviceapi.exception.StorageException;
import com.netdatel.documentserviceapi.model.dto.request.FileUploadRequest;
import com.netdatel.documentserviceapi.model.entity.ClientSpace;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.FileVersion;
import com.netdatel.documentserviceapi.model.entity.Folder;
import com.netdatel.documentserviceapi.model.enums.FileStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatusColor;
import com.netdatel.documentserviceapi.repository.FileRepository;
import com.netdatel.documentserviceapi.repository.FileVersionRepository;
import com.netdatel.documentserviceapi.repository.FolderRepository;
import com.netdatel.documentserviceapi.service.ClientSpaceService;
import com.netdatel.documentserviceapi.service.FileService;
import com.netdatel.documentserviceapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final ClientSpaceService clientSpaceService;

    @Override
    public File uploadFile(FileUploadRequest request, InputStream fileContent,
                           long fileSize, String contentType, Integer userId) throws IOException {
        log.info("Uploading file to folder: {}", request.getFolderId());

        // Verificar que la carpeta existe
        Folder folder = folderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));

        // Verificar espacio disponible
        checkStorageQuota(folder.getClientSpace().getId(), fileSize);

        // Verificar si el nombre ya existe en la carpeta
        if (fileRepository.existsByFolderIdAndName(request.getFolderId(), request.getName())) {
            throw new InvalidRequestException("Ya existe un archivo con ese nombre en la carpeta");
        }

        // Generar clave única para almacenamiento
        String storageKey = generateStorageKey(folder.getClientSpace().getId(),
                folder.getClientSpace().getModuleId(),
                request.getName());

        // Subir archivo a MinIO
        storageService.uploadFile(fileContent, fileSize, storageKey, contentType, Collections.emptyMap());

        // Convertir metadatos a JSON
        String metadataJson = null;
        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            try {
                metadataJson = new ObjectMapper().writeValueAsString(request.getMetadata());
            } catch (Exception e) {
                log.warn("Error converting metadata to JSON", e);
                metadataJson = "{}";
            }
        }

        // Crear registro en la base de datos
        File file = File.builder()
                .folder(folder)
                .name(request.getName())
                .originalName(request.getName())
                .fileSize(fileSize)
                .mimeType(contentType)
                .storagePath(folder.getPath())
                .storageKey(storageKey)
                .status(FileStatus.ACTIVE)
                .viewStatus(ViewStatus.NEW)
                .viewStatusColor(ViewStatusColor.BLUE)
                .uploadedBy(userId)
                .version(1)
                .metadata(metadataJson)
                .build();

        File savedFile = fileRepository.save(file);

        // Actualizar espacio utilizado
        updateClientSpaceUsedBytes(folder.getClientSpace().getId(), fileSize, true);

        return savedFile;
    }

    @Override
    public File getFile(Integer id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));
    }

    @Override
    public List<File> getFilesByFolder(Integer folderId, FileStatus status) {
        if (!folderRepository.existsById(folderId)) {
            throw new ResourceNotFoundException("Carpeta no encontrada");
        }

        return fileRepository.findByFolderIdAndStatus(folderId, status);
    }

    @Override
    public File updateFile(Integer id, FileUploadRequest request, Integer userId) {
        log.info("Updating file: {}", id);

        File file = getFile(id);

        // Actualizar nombre si se proporciona
        if (request.getName() != null && !request.getName().isEmpty() && !request.getName().equals(file.getName())) {
            // Verificar si el nuevo nombre ya existe en la carpeta
            if (fileRepository.existsByFolderIdAndName(file.getFolder().getId(), request.getName())) {
                throw new InvalidRequestException("Ya existe un archivo con ese nombre en la carpeta");
            }

            file.setName(request.getName());
        }

        // Actualizar metadatos si se proporcionan
        if (request.getMetadata() != null) {
            try {
                String metadataJson = new ObjectMapper().writeValueAsString(request.getMetadata());
                file.setMetadata(metadataJson);
            } catch (Exception e) {
                log.warn("Error converting metadata to JSON", e);
            }
        }

        return fileRepository.save(file);
    }

    @Override
    public File uploadNewVersion(Integer id, InputStream fileContent,
                                 long fileSize, String contentType,
                                 String comments, Integer userId) throws IOException {
        log.info("Uploading new version for file: {}", id);

        File file = getFile(id);

        // Verificar espacio disponible
        checkStorageQuota(file.getFolder().getClientSpace().getId(), fileSize);

        // Guardar versión anterior
        FileVersion fileVersion = FileVersion.builder()
                .file(file)
                .versionNumber(file.getVersion())
                .fileSize(file.getFileSize())
                .storagePath(file.getStoragePath())
                .storageKey(file.getStorageKey())
                .createdBy(userId)
                .changeComments(comments)
                .build();

        fileVersionRepository.save(fileVersion);

        // Generar nueva clave de almacenamiento
        String newStorageKey = generateStorageKey(file.getFolder().getClientSpace().getId(),
                file.getFolder().getClientSpace().getModuleId(),
                file.getName());

        // Subir nueva versión a MinIO
        storageService.uploadFile(fileContent, fileSize, newStorageKey, contentType, Collections.emptyMap());

        // Actualizar registro en la base de datos
        long oldSize = file.getFileSize();
        file.setFileSize(fileSize);
        file.setStorageKey(newStorageKey);
        file.setMimeType(contentType);
        file.setVersion(file.getVersion() + 1);

        File updatedFile = fileRepository.save(file);

        // Actualizar espacio utilizado (restar tamaño anterior y sumar nuevo)
        updateClientSpaceUsedBytes(file.getFolder().getClientSpace().getId(), fileSize - oldSize, true);

        return updatedFile;
    }

    @Override
    public void deleteFile(Integer id, Integer userId) {
        log.info("Deleting file: {}", id);

        File file = getFile(id);

        // Cambiar estado a DELETED
        file.setStatus(FileStatus.DELETED);

        fileRepository.save(file);

        // No eliminar físicamente de MinIO, solo marcar como eliminado en la base de datos
        // Esto permite recuperación y cumple con requisitos de auditoría

        // Actualizar espacio utilizado
        updateClientSpaceUsedBytes(file.getFolder().getClientSpace().getId(), -file.getFileSize(), true);
    }

    @Override
    public List<FileVersion> getFileVersions(Integer fileId) {
        if (!fileRepository.existsById(fileId)) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }

        return fileVersionRepository.findByFileIdOrderByVersionNumberDesc(fileId);
    }

    @Override
    public void updateFileViewStatus(Integer id, ViewStatus viewStatus, ViewStatusColor viewStatusColor) {
        log.info("Updating file view status: {} to {}", id, viewStatus);

        File file = getFile(id);
        file.setViewStatus(viewStatus);
        file.setViewStatusColor(viewStatusColor);

        if (viewStatus == ViewStatus.VIEWED && file.getLastViewedDate() == null) {
            file.setLastViewedDate(LocalDateTime.now());
        } else if (viewStatus == ViewStatus.DOWNLOADED) {
            file.setLastDownloadedDate(LocalDateTime.now());
        }

        fileRepository.save(file);
    }

    @Override
    public Page<File> searchFiles(String query, Integer clientId, Pageable pageable) {
        if (clientId != null) {
            return fileRepository.searchByNameAndClientId(clientId, query, pageable);
        } else {
            return fileRepository.findByNameContainingIgnoreCase(query, pageable);
        }
    }

    // Helper methods

    private String generateStorageKey(Integer clientSpaceId, Integer moduleId, String fileName) {
        LocalDateTime now = LocalDateTime.now();
        String datePath = String.format("%d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String uniqueId = UUID.randomUUID().toString();

        return String.format("clients/%d/module_%d/%s/%s_%s",
                clientSpaceId, moduleId, datePath, uniqueId, fileName);
    }

    private void checkStorageQuota(Integer clientSpaceId, long fileSize) {
        ClientSpace clientSpace = clientSpaceService.getClientSpace(clientSpaceId);

        if (clientSpace.getUsedBytes() + fileSize > clientSpace.getTotalQuotaBytes()) {
            throw new StorageException("Cuota de almacenamiento excedida");
        }
    }

    private void updateClientSpaceUsedBytes(Integer clientSpaceId, long bytesChange, boolean absolute) {
        ClientSpace clientSpace = clientSpaceService.getClientSpace(clientSpaceId);

        long newUsedBytes;
        if (absolute) {
            newUsedBytes = clientSpace.getUsedBytes() + bytesChange;
        } else {
            newUsedBytes = bytesChange;
        }

        // Asegurar que no sea negativo
        if (newUsedBytes < 0) {
            newUsedBytes = 0;
        }

        clientSpaceService.updateUsedBytes(clientSpaceId, newUsedBytes);
    }
}
