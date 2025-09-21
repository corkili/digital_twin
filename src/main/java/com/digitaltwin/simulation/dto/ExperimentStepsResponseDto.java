package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 试验步骤统一响应DTO
 * 包含手动模式步骤数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "试验步骤统一响应信息")
public class ExperimentStepsResponseDto {

    @Schema(description = "试验名称", example = "传感器校准试验")
    private String experimentName;

    @Schema(description = "手动模式步骤数据")
    private ExperimentStepsDto manualSteps;
}