package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 试验步骤DTO
 * 对应新的JSON结构格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "试验步骤信息")
public class ExperimentStepDto {
    
    @Schema(description = "步骤ID", example = "1")
    private Integer stepId;
    
    @Schema(description = "步骤名称", example = "传感器初始化")
    private String stepName;
    
    @Schema(description = "步骤中包含的角色列表")
    private List<RoleDto> roles;
}