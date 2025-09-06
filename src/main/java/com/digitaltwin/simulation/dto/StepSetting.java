package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "步骤设置信息")
public class StepSetting {
    
    @Schema(description = "前缀文本", example = "设置电动阀开度值:")
    private String prefix;

    @Schema(description = "设置值", example = "50%")
    private String value;

    @Schema(description = "后缀文本", example = "(按需)")
    private String suffix;
}
