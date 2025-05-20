package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.exception.StorageException;
import io.minio.StatObjectResponse;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
public class DummyStorageService implements StorageService {
    @Override
    public String uploadFile(InputStream fileData, long size, String objectKey, String contentType, Map<String, String> metadata) {
        return null;
    }

    @Override
    public byte[] downloadFile(String objectKey) {
        return new byte[0];
    }

    @Override
    public String generatePresignedUrl(String objectKey, int expiryMinutes) {
        return null;
    }

    @Override
    public void deleteFile(String objectKey) {

    }

    @Override
    public boolean bucketExists(String bucketName) {
        return false;
    }

    @Override
    public void createBucket(String bucketName) {

    }

    @Override
    public long getFileSize(String objectKey) throws StorageException {
        return 0;
    }

    @Override
    public StatObjectResponse getObjectStat(String objectKey) throws StorageException {
        return null;
    }
    // métodos vacíos
}