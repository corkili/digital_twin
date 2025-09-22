package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 修改试验步骤请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改试验步骤请求")
public class UpdateExperimentStepsRequest {
    
    @NotNull(message = "试验ID不能为空")
    @Schema(description = "试验ID", example = "1", required = true)
    private Long experimentId;
    
    @NotEmpty(message = "试验步骤不能为空")
    @Schema(description = "试验步骤列表", required = true)
    private List<ExperimentStepDto> steps;
}