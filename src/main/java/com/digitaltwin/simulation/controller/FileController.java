package com.digitaltwin.simulation.controller;

import com.digitaltwin.simulation.config.MinIOConfig;
import com.digitaltwin.simulation.dto.PreSignedUrlRequest;
import com.digitaltwin.simulation.dto.PreSignedUrlResponse;
import com.digitaltwin.simulation.dto.SimulationApiResponse;
import com.digitaltwin.simulation.service.MinIOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 文件管理控制器
 * 提供MinIO文件存储相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/simulations/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "提供MinIO文件存储服务的预签名URL接口")
public class FileController {

    private final MinIOService minIOService;
    private final MinIOConfig.MinIOProperties minIOProperties;

    /**
     * 生成预签名URL
     * 
     * @param request 预签名URL请求参数
     * @return 预签名URL响应
     */
    @Operation(summary = "生成预签名URL", description = "为文件上传或下载生成预签名URL")
    @PostMapping("/presigned-url")
    public ResponseEntity<SimulationApiResponse<PreSignedUrlResponse>> generatePresignedUrl(
            @Valid @RequestBody PreSignedUrlRequest request) {
        try {
            log.info("生成预签名URL请求: fileName={}, operationType={}, expiry={}", 
                    request.getFileName(), request.getOperationType(), request.getExpiry());
            
            String presignedUrl;
            if (request.getOperationType() == PreSignedUrlRequest.OperationType.UPLOAD) {
                presignedUrl = minIOService.generatePresignedPutUrl(request.getFileName(), request.getExpiry());
            } else {
                presignedUrl = minIOService.generatePresignedGetUrl(request.getFileName(), request.getExpiry());
            }
            
            PreSignedUrlResponse response = PreSignedUrlResponse.builder()
                    .presignedUrl(presignedUrl)
                    .fileName(request.getFileName())
                    .operationType(request.getOperationType())
                    .expiryTime(LocalDateTime.now().plusMinutes(request.getExpiry()))
                    .bucketName(minIOProperties.getBucketName())
                    .contentType(request.getContentType())
                    .build();
            
            log.info("预签名URL生成成功: fileName={}, operationType={}", 
                    request.getFileName(), request.getOperationType());
            
            return ResponseEntity.ok(SimulationApiResponse.success("预签名URL生成成功", response));
            
        } catch (Exception e) {
            log.error("生成预签名URL失败: fileName={}, operationType={}, error={}", 
                    request.getFileName(), request.getOperationType(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("生成预签名URL失败: " + e.getMessage()));
        }
    }

    /**
     * 获取MinIO服务器信息
     * 
     * @return MinIO服务器配置信息
     */
    @Operation(summary = "获取MinIO服务器信息", description = "获取MinIO服务器的基本配置信息")
    @GetMapping("/server-info")
    public ResponseEntity<SimulationApiResponse<MinIOServerInfo>> getServerInfo() {
        try {
            MinIOServerInfo serverInfo = MinIOServerInfo.builder()
                    .endpoint(minIOProperties.getEndpoint())
                    .bucketName(minIOProperties.getBucketName())
                    .region(minIOProperties.getRegion())
                    .build();
            
            return ResponseEntity.ok(SimulationApiResponse.success("获取服务器信息成功", serverInfo));
            
        } catch (Exception e) {
            log.error("获取MinIO服务器信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取服务器信息失败: " + e.getMessage()));
        }
    }

    /**
     * MinIO服务器信息DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "MinIO服务器信息")
    public static class MinIOServerInfo {
        @io.swagger.v3.oas.annotations.media.Schema(description = "服务器端点")
        private String endpoint;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "存储桶名称")
        private String bucketName;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "区域")
        private String region;
    }
}