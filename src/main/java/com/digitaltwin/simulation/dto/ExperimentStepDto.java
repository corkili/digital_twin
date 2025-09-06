package com.digitaltwin.simulation.dto;

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
public class ExperimentStepDto {
    
    private Integer stepId;           // 步骤ID
    private String stepName;          // 步骤名称
    private List<RoleDto> roles;      // 角色列表
}