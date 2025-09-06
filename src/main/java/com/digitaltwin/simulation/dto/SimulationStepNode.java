package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 仿真步骤节点 - 全量功能节点
 * 支持基础节点、设置节点、复选框组节点、输入项组节点等所有类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仿真步骤节点（支持子节点、设置、复选框、输入项）")
public class SimulationStepNode {
    
    @Schema(description = "节点名称", example = "设置参数")
    private String name;

    @Schema(description = "唯一标识（UE/业务ID）", example = "ue_setting_001")
    private String ue;

    @Schema(description = "子节点数组")
    private List<SimulationStepNode> child;
    
    @Schema(description = "设置配置（当节点为设置类型时有效)")
    private StepSetting setting;
    
    @Schema(description = "复选框组（当节点为复选框类型时有效)")
    private List<CheckBoxItem> checkBoxs;
    
    @Schema(description = "输入项组（当节点为输入项类型时有效)")
    private List<InputItem> inputList;
}