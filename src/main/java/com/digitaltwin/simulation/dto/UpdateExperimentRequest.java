package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 修改试验请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改试验请求")
public class UpdateExperimentRequest {

    @NotNull(message = "试验ID不能为空")
    @Schema(description = "试验ID", example = "1", required = true)
    private Long experimentId;

    @NotBlank(message = "试验名称不能为空")
    @Size(max = 255, message = "试验名称长度不能超过255个字符")
    @Schema(description = "试验名称", example = "传感器校准试验", required = true)
    private String name;

    @Schema(description = "试验详细描述", example = "该试验旨在验证传感器在不同环境下的校准精度和稳定性")
    private String description;

    @Schema(description = "试验步骤列表")
    private List<ExperimentStepDto> steps;
}