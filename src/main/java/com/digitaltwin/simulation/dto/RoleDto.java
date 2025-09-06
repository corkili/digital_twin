package com.digitaltwin.simulation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 角色DTO
 * 用于试验步骤中的角色信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    
    private String roleId;                    // 角色ID
    private String roleName;                  // 角色名称
    private List<SimulationStepNode> tasks;   // 该角色的任务节点列表
}