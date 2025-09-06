package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 输入项
 * 用于输入项组功能的节点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "输入项")
public class InputItem {
    
    @Schema(description = "前缀1", example = "打开:")
    private String prefix1;

    @Schema(description = "值1", example = "")
    private String value1;

    @Schema(description = "前缀2", example = "转速")
    private String prefix2;

    @Schema(description = "值2", example = "3000rpm")
    private String value2;

    @Schema(description = "按钮文本", example = "启动")
    private String button;
}
