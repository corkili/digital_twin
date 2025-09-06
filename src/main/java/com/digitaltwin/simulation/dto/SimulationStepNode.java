package com.digitaltwin.simulation.dto;

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
public class SimulationStepNode {
    
    // 基础属性
    private String name;           // 节点名称
    private String ue;             // 唯一标识
    private List<SimulationStepNode> child;  // 子节点数组
    
    // 设置功能
    private StepSetting setting;  // 设置配置
    
    // 复选框功能  
    private List<CheckBoxItem> checkBoxs;  // 复选框组
    
    // 输入项功能
    private List<InputItem> inputList;     // 输入项组
}