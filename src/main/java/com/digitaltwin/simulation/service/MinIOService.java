package com.digitaltwin.simulation.service;

import com.digitaltwin.simulation.config.MinIOConfig;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOService {

    private final MinioClient minioClient;
    private final MinIOConfig.MinIOProperties minIOProperties;

    /**
     * 生成预签名上传URL
     * 
     * @param objectName 对象名称（文件路径）
     * @param expiry 过期时间（分钟）
     * @return 预签名上传URL
     */
    public String generatePresignedPutUrl(String objectName, int expiry) {
        try {
            ensureBucketExists();
            
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minIOProperties.getBucketName())
                            .object(objectName)
                            .expiry(expiry, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名上传URL失败: objectName={}, expiry={}", objectName, expiry, e);
            throw new RuntimeException("生成预签名上传URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成预签名下载URL
     * 
     * @param objectName 对象名称（文件路径）
     * @param expiry 过期时间（分钟）
     * @return 预签名下载URL
     */
    public String generatePresignedGetUrl(String objectName, int expiry) {
        try {
            ensureBucketExists();
            
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minIOProperties.getBucketName())
                            .object(objectName)
                            .expiry(expiry, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名下载URL失败: objectName={}, expiry={}", objectName, expiry, e);
            throw new RuntimeException("生成预签名下载URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 确保存储桶存在，如果不存在则创建
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minIOProperties.getBucketName())
                            .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minIOProperties.getBucketName())
                                .build()
                );
                log.info("创建存储桶: {}", minIOProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("检查或创建存储桶失败: bucket={}", minIOProperties.getBucketName(), e);
            throw new RuntimeException("检查或创建存储桶失败: " + e.getMessage(), e);
        }
    }
}