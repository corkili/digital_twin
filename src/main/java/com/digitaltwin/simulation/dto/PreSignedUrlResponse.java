package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "预签名URL响应")
public class PreSignedUrlResponse {

    @Schema(description = "预签名URL")
    private String presignedUrl;

    @Schema(description = "文件名或对象路径")
    private String fileName;

    @Schema(description = "操作类型")
    private PreSignedUrlRequest.OperationType operationType;

    @Schema(description = "过期时间")
    private LocalDateTime expiryTime;

    @Schema(description = "存储桶名称")
    private String bucketName;

    @Schema(description = "内容类型")
    private String contentType;
}