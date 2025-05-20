package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.exception.StorageException;
import io.minio.StatObjectResponse;

import java.io.InputStream;
import java.util.Map;

public interface StorageService {
    String uploadFile(InputStream fileData, long size, String objectKey, String contentType, Map<String, String> metadata);
    byte[] downloadFile(String objectKey);
    String generatePresignedUrl(String objectKey, int expiryMinutes);
    void deleteFile(String objectKey);
    boolean bucketExists(String bucketName);
    void createBucket(String bucketName);
    long getFileSize(String objectKey) throws StorageException;
    StatObjectResponse getObjectStat(String objectKey) throws StorageException;
}