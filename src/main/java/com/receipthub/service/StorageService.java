package com.receipthub.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageService {
    
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;

    private void initializeBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());

            if (!exists) {
                log.info("Creating MinIO bucket: {}", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO bucket created successfully: {}", bucketName);
            }
            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": "*"},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """, bucketName);
            
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build());
            
            log.info("MinIO bucket policy set to allow public read access");
            
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket", e);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }
    
    public String uploadFile(byte[] fileData, String originalFileName) throws IOException {
        try {
            initializeBucket();
            
            String extension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf("."))
                    : "";
            String objectName = UUID.randomUUID().toString() + extension;
            
            log.info("Uploading byte array to MinIO: {} (original: {})", objectName, originalFileName);
            
            ByteArrayInputStream stream = new ByteArrayInputStream(fileData);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(stream, fileData.length, -1)
                            .contentType("application/octet-stream")
                            .build());
            
            log.info("Byte array uploaded successfully to MinIO: {}", objectName);
            return objectName;
            
        } catch (Exception e) {
            log.error("Error uploading byte array to MinIO", e);
            throw new IOException("Failed to upload byte array to MinIO", e);
        }
    }
    
    public byte[] getFile(String objectNameOrPath) throws IOException {
        try {

            String objectName = objectNameOrPath;
            if (objectNameOrPath.contains("/")) {
                objectName = Paths.get(objectNameOrPath).getFileName().toString();
                log.info("Converted path '{}' to object name '{}'", objectNameOrPath, objectName);
            }
            
            log.info("Downloading file from MinIO: {}", objectName);
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return stream.readAllBytes();
        } catch (Exception e) {
            log.warn("MinIO download failed for '{}', attempting local file system fallback", objectNameOrPath);
            try {
                Path localPath = Paths.get(objectNameOrPath);
                if (Files.exists(localPath)) {
                    log.info("Reading file from local filesystem: {}", objectNameOrPath);
                    return Files.readAllBytes(localPath);
                }
            } catch (Exception localEx) {
                log.error("Local filesystem fallback also failed for: {}", objectNameOrPath, localEx);
            }
            
            log.error("Error downloading file from MinIO and local fallback failed: {}", objectNameOrPath, e);
            throw new IOException("Failed to download file from MinIO or local storage", e);
        }
    }
}

