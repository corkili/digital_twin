package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 试验步骤列表DTO
 * 包含完整的试验步骤数组
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "试验步骤列表信息")
public class ExperimentStepsDto {
    
    @Schema(description = "试验步骤列表")
    private List<ExperimentStepDto> steps;
    
    @Schema(description = "试验总步骤数", example = "5")
    private Integer totalSteps;
    
    @Schema(description = "试验名称", example = "传感器数据采集实验")
    private String experimentName;
}