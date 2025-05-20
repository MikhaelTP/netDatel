package com.netdatel.documentserviceapi.service.impl;

import com.netdatel.documentserviceapi.exception.ResourceNotFoundException;
import com.netdatel.documentserviceapi.model.dto.request.PermissionRequest;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.FilePermission;
import com.netdatel.documentserviceapi.model.entity.Folder;
import com.netdatel.documentserviceapi.model.entity.FolderPermission;
import com.netdatel.documentserviceapi.repository.FilePermissionRepository;
import com.netdatel.documentserviceapi.repository.FileRepository;
import com.netdatel.documentserviceapi.repository.FolderPermissionRepository;
import com.netdatel.documentserviceapi.repository.FolderRepository;
import com.netdatel.documentserviceapi.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final FolderPermissionRepository folderPermissionRepository;
    private final FilePermissionRepository filePermissionRepository;

    // Folder permissions

    @Override
    public FolderPermission assignFolderPermission(Integer folderId, PermissionRequest request, Integer grantedBy) {
        log.info("Assigning folder permission for user: {} to folder: {}", request.getUserId(), folderId);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));

        // Verificar si ya existe un permiso, en ese caso actualizarlo
        Optional<FolderPermission> existingPermission = folderPermissionRepository
                .findByFolderIdAndUserId(folderId, request.getUserId());

        if (existingPermission.isPresent()) {
            FolderPermission permission = existingPermission.get();
            updatePermissionValues(permission, request);
            permission.setGrantedBy(grantedBy);
            return folderPermissionRepository.save(permission);
        }

        // Si no existe, crear nuevo permiso
        FolderPermission permission = FolderPermission.builder()
                .folder(folder)
                .userId(request.getUserId())
                .canRead(request.isCanRead())
                .canWrite(request.isCanWrite())
                .canDelete(request.isCanDelete())
                .canDownload(request.isCanDownload())
                .grantedBy(grantedBy)
                .validUntil(request.getValidUntil())
                .isActive(true)
                .build();

        return folderPermissionRepository.save(permission);
    }

    @Override
    public List<FolderPermission> getFolderPermissions(Integer folderId) {
        log.info("Getting permissions for folder: {}", folderId);

        if (!folderRepository.existsById(folderId)) {
            throw new ResourceNotFoundException("Carpeta no encontrada");
        }

        return folderPermissionRepository.findByFolderId(folderId);
    }

    @Override
    public FolderPermission getFolderPermission(Integer id) {
        return folderPermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado"));
    }

    @Override
    public FolderPermission updateFolderPermission(Integer id, PermissionRequest request, Integer updatedBy) {
        log.info("Updating folder permission: {}", id);

        FolderPermission permission = getFolderPermission(id);
        updatePermissionValues(permission, request);
        permission.setGrantedBy(updatedBy);

        return folderPermissionRepository.save(permission);
    }

    @Override
    public void revokeFolderPermission(Integer id) {
        log.info("Revoking folder permission: {}", id);

        FolderPermission permission = getFolderPermission(id);
        permission.setActive(false);

        folderPermissionRepository.save(permission);
    }

    // File permissions

    @Override
    public FilePermission assignFilePermission(Integer fileId, PermissionRequest request, Integer grantedBy) {
        log.info("Assigning file permission for user: {} to file: {}", request.getUserId(), fileId);

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

        // Verificar si ya existe un permiso, en ese caso actualizarlo
        Optional<FilePermission> existingPermission = filePermissionRepository
                .findByFileIdAndUserId(fileId, request.getUserId());

        if (existingPermission.isPresent()) {
            FilePermission permission = existingPermission.get();
            updatePermissionValues(permission, request);
            permission.setGrantedBy(grantedBy);
            return filePermissionRepository.save(permission);
        }

        // Si no existe, crear nuevo permiso
        FilePermission permission = FilePermission.builder()
                .file(file)
                .userId(request.getUserId())
                .canRead(request.isCanRead())
                .canWrite(request.isCanWrite())
                .canDelete(request.isCanDelete())
                .canDownload(request.isCanDownload())
                .grantedBy(grantedBy)
                .validUntil(request.getValidUntil())
                .isActive(true)
                .build();

        return filePermissionRepository.save(permission);
    }

    @Override
    public List<FilePermission> getFilePermissions(Integer fileId) {
        log.info("Getting permissions for file: {}", fileId);

        if (!fileRepository.existsById(fileId)) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }

        return filePermissionRepository.findByFileId(fileId);
    }

    @Override
    public FilePermission getFilePermission(Integer id) {
        return filePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado"));
    }

    @Override
    public FilePermission updateFilePermission(Integer id, PermissionRequest request, Integer updatedBy) {
        log.info("Updating file permission: {}", id);

        FilePermission permission = getFilePermission(id);
        updatePermissionValues(permission, request);
        permission.setGrantedBy(updatedBy);

        return filePermissionRepository.save(permission);
    }

    @Override
    public void revokeFilePermission(Integer id) {
        log.info("Revoking file permission: {}", id);

        FilePermission permission = getFilePermission(id);
        permission.setActive(false);

        filePermissionRepository.save(permission);
    }

    // Permission verification methods

    @Override
    public boolean userCanReadFolder(Integer userId, Integer folderId) {
        try {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));

            // Verificar permiso directo en la carpeta
            Optional<FolderPermission> permission = folderPermissionRepository.findByFolderIdAndUserId(folderId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanRead()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar en carpetas padres
            if (folder.getParent() != null) {
                return userCanReadFolder(userId, folder.getParent().getId());
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying read permission for user: {} in folder: {}", userId, folderId, e);
            return false;
        }
    }

    @Override
    public boolean userCanWriteFolder(Integer userId, Integer folderId) {
        try {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));

            // Verificar permiso directo en la carpeta
            Optional<FolderPermission> permission = folderPermissionRepository.findByFolderIdAndUserId(folderId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanWrite()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar en carpetas padres
            if (folder.getParent() != null) {
                return userCanWriteFolder(userId, folder.getParent().getId());
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying write permission for user: {} in folder: {}", userId, folderId, e);
            return false;
        }
    }

    @Override
    public boolean userCanDeleteFolder(Integer userId, Integer folderId) {
        try {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));

            // Verificar permiso directo en la carpeta
            Optional<FolderPermission> permission = folderPermissionRepository.findByFolderIdAndUserId(folderId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanDelete()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar en carpetas padres
            if (folder.getParent() != null) {
                return userCanDeleteFolder(userId, folder.getParent().getId());
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying delete permission for user: {} in folder: {}", userId, folderId, e);
            return false;
        }
    }

    @Override
    public boolean userCanDownloadFolder(Integer userId, Integer folderId) {
        try {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));

            // Verificar permiso directo en la carpeta
            Optional<FolderPermission> permission = folderPermissionRepository.findByFolderIdAndUserId(folderId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanDownload()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar en carpetas padres
            if (folder.getParent() != null) {
                return userCanDownloadFolder(userId, folder.getParent().getId());
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying download permission for user: {} in folder: {}", userId, folderId, e);
            return false;
        }
    }

    @Override
    public boolean userCanReadFile(Integer userId, Integer fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

            // Verificar permiso directo en el archivo
            Optional<FilePermission> permission = filePermissionRepository.findByFileIdAndUserId(fileId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanRead()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar permiso en la carpeta contenedora
            return userCanReadFolder(userId, file.getFolder().getId());
        } catch (Exception e) {
            log.error("Error verifying read permission for user: {} in file: {}", userId, fileId, e);
            return false;
        }
    }

    @Override
    public boolean userCanWriteFile(Integer userId, Integer fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

            // Verificar permiso directo en el archivo
            Optional<FilePermission> permission = filePermissionRepository.findByFileIdAndUserId(fileId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanWrite()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar permiso en la carpeta contenedora
            return userCanWriteFolder(userId, file.getFolder().getId());
        } catch (Exception e) {
            log.error("Error verifying write permission for user: {} in file: {}", userId, fileId, e);
            return false;
        }
    }

    @Override
    public boolean userCanDeleteFile(Integer userId, Integer fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

            // Verificar permiso directo en el archivo
            Optional<FilePermission> permission = filePermissionRepository.findByFileIdAndUserId(fileId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanDelete()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar permiso en la carpeta contenedora
            return userCanDeleteFolder(userId, file.getFolder().getId());
        } catch (Exception e) {
            log.error("Error verifying delete permission for user: {} in file: {}", userId, fileId, e);
            return false;
        }
    }

    @Override
    public boolean userCanDownloadFile(Integer userId, Integer fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

            // Verificar permiso directo en el archivo
            Optional<FilePermission> permission = filePermissionRepository.findByFileIdAndUserId(fileId, userId);
            if (permission.isPresent() && permission.get().isActive() && permission.get().isCanDownload()) {
                return true;
            }

            // Si no tiene permiso directo, comprobar permiso en la carpeta contenedora
            return userCanDownloadFolder(userId, file.getFolder().getId());
        } catch (Exception e) {
            log.error("Error verifying download permission for user: {} in file: {}", userId, fileId, e);
            return false;
        }
    }

    private void updatePermissionValues(FolderPermission permission, PermissionRequest request) {
        permission.setCanRead(request.isCanRead());
        permission.setCanWrite(request.isCanWrite());
        permission.setCanDelete(request.isCanDelete());
        permission.setCanDownload(request.isCanDownload());
        permission.setValidUntil(request.getValidUntil());
    }

    private void updatePermissionValues(FilePermission permission, PermissionRequest request) {
        permission.setCanRead(request.isCanRead());
        permission.setCanWrite(request.isCanWrite());
        permission.setCanDelete(request.isCanDelete());
        permission.setCanDownload(request.isCanDownload());
        permission.setValidUntil(request.getValidUntil());
    }
}