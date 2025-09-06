package com.digitaltwin.simulation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 步骤设置信息
 * 用于包含设置配置的节点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepSetting {
    
    private String prefix;    // 前缀文本，如："设置电动阀开度值:"
    private String value;     // 设置值，如：""
    private String suffix;    // 后缀文本，如："(按需)"
}