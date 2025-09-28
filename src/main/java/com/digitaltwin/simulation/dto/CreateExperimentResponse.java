package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建试验响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建试验响应")
public class CreateExperimentResponse {

    @Schema(description = "试验ID", example = "1")
    private Long id;

    @Schema(description = "试验名称", example = "传感器校准试验")
    private String name;

    @Schema(description = "试验描述", example = "该试验旨在验证传感器在不同环境下的校准精度和稳定性")
    private String description;

    @Schema(description = "试验状态", example = "ACTIVE")
    private String status;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "是否包含步骤数据", example = "true")
    private Boolean hasStepsData;
}