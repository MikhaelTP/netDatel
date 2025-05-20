package com.netdatel.documentserviceapi.service.impl;

import com.netdatel.documentserviceapi.config.MinioProperties;
import com.netdatel.documentserviceapi.exception.StorageException;
import com.netdatel.documentserviceapi.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MinioStorageServiceImpl implements StorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void init() {
        try {
            createBucketIfNotExists(minioProperties.getBucketName());
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket", e);
            throw new StorageException("Could not initialize MinIO storage", e);
        }
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        if (!bucketExists(bucketName)) {
            createBucket(bucketName);
        }
    }

    @Override
    public String uploadFile(InputStream fileData, long size, String objectKey,
                             String contentType, Map<String, String> metadata) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(fileData, size, -1)
                    .contentType(contentType)
                    .build();

            if (metadata != null && !metadata.isEmpty()) {
                metadata.forEach((key, value) -> args.headers().put(key, value));
            }

            minioClient.putObject(args);
            log.info("File uploaded successfully to MinIO: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("Error uploading file to MinIO", e);
            throw new StorageException("Could not upload file", e);
        }
    }

    @Override
    public byte[] downloadFile(String objectKey) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build();

            try (GetObjectResponse response = minioClient.getObject(args)) {
                return IOUtils.toByteArray(response);
            }
        } catch (Exception e) {
            log.error("Error downloading file from MinIO", e);
            throw new StorageException("Could not download file", e);
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, int expiryMinutes) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            throw new StorageException("Could not generate presigned URL", e);
        }
    }

    @Override
    public void deleteFile(String objectKey) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build();

            minioClient.removeObject(args);
            log.info("File deleted successfully from MinIO: {}", objectKey);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO", e);
            throw new StorageException("Could not delete file", e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("Error checking if bucket exists", e);
            throw new StorageException("Could not check if bucket exists", e);
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket created successfully: {}", bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket", e);
            throw new StorageException("Could not create bucket", e);
        }
    }

    @Override
    public long getFileSize(String objectKey) throws StorageException {
        try {
            StatObjectResponse stat = getObjectStat(objectKey);
            return stat.size();
        } catch (Exception e) {
            log.error("Error getting file size", e);
            throw new StorageException("Could not get file size", e);
        }
    }

    @Override
    public StatObjectResponse getObjectStat(String objectKey) throws StorageException {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build();

            return minioClient.statObject(args);
        } catch (Exception e) {
            log.error("Error getting object stats", e);
            throw new StorageException("Could not get object stats", e);
        }
    }
}