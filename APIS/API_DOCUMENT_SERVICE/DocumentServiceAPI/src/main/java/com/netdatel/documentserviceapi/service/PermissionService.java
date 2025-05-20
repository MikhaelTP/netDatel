package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.model.dto.request.PermissionRequest;
import com.netdatel.documentserviceapi.model.entity.FilePermission;
import com.netdatel.documentserviceapi.model.entity.FolderPermission;

import java.util.List;

public interface PermissionService {
    // Folder permissions
    FolderPermission assignFolderPermission(Integer folderId, PermissionRequest request, Integer grantedBy);
    List<FolderPermission> getFolderPermissions(Integer folderId);
    FolderPermission getFolderPermission(Integer id);
    FolderPermission updateFolderPermission(Integer id, PermissionRequest request, Integer updatedBy);
    void revokeFolderPermission(Integer id);

    // File permissions
    FilePermission assignFilePermission(Integer fileId, PermissionRequest request, Integer grantedBy);
    List<FilePermission> getFilePermissions(Integer fileId);
    FilePermission getFilePermission(Integer id);
    FilePermission updateFilePermission(Integer id, PermissionRequest request, Integer updatedBy);
    void revokeFilePermission(Integer id);

    // Verification methods
    boolean userCanReadFolder(Integer userId, Integer folderId);
    boolean userCanWriteFolder(Integer userId, Integer folderId);
    boolean userCanDeleteFolder(Integer userId, Integer folderId);
    boolean userCanDownloadFolder(Integer userId, Integer folderId);

    boolean userCanReadFile(Integer userId, Integer fileId);
    boolean userCanWriteFile(Integer userId, Integer fileId);
    boolean userCanDeleteFile(Integer userId, Integer fileId);
    boolean userCanDownloadFile(Integer userId, Integer fileId);
}