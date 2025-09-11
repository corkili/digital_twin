package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "预签名URL请求")
public class PreSignedUrlRequest {

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "文件名或对象路径", example = "experiments/2023/data.xlsx")
    private String fileName;

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型", allowableValues = {"UPLOAD", "DOWNLOAD"})
    private OperationType operationType;

    @Min(value = 1, message = "过期时间最少为1分钟")
    @Max(value = 1440, message = "过期时间最多为1440分钟（24小时）")
    @Schema(description = "过期时间（分钟）", example = "60", defaultValue = "60")
    private int expiry = 60;

    @Schema(description = "内容类型", example = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    private String contentType;

    public enum OperationType {
        UPLOAD, DOWNLOAD
    }
}