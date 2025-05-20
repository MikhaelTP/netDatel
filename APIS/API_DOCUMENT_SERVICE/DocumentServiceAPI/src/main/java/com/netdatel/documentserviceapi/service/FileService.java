package com.netdatel.documentserviceapi.service;


import com.netdatel.documentserviceapi.model.dto.request.FileUploadRequest;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.FileVersion;
import com.netdatel.documentserviceapi.model.enums.FileStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatus;
import com.netdatel.documentserviceapi.model.enums.ViewStatusColor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileService {
    File uploadFile(FileUploadRequest request, InputStream fileContent, long fileSize, String contentType, Integer userId) throws IOException;
    File getFile(Integer id);
    List<File> getFilesByFolder(Integer folderId, FileStatus status);
    File updateFile(Integer id, FileUploadRequest request, Integer userId);
    File uploadNewVersion(Integer id, InputStream fileContent, long fileSize, String contentType, String comments, Integer userId) throws IOException;
    void deleteFile(Integer id, Integer userId);
    List<FileVersion> getFileVersions(Integer fileId);
    void updateFileViewStatus(Integer id, ViewStatus viewStatus, ViewStatusColor viewStatusColor);
    Page<File> searchFiles(String query, Integer clientId, Pageable pageable);
}
