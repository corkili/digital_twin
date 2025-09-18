package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 试验步骤统一响应DTO
 * 包含手动模式步骤和自动模式的两个流程，由前端选择展示
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

    @Schema(description = "自动模式试验流程")
    private List<SimulationStepNode> experimentFlow;

    @Schema(description = "自动模式应急流程")
    private List<SimulationStepNode> emergencyFlow;
}