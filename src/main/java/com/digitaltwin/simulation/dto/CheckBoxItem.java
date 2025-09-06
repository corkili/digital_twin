package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 复选框项
 * 用于复选框组功能的节点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "复选框项")
public class CheckBoxItem {
    
    @Schema(description = "复选框名称", example = "氮气")
    private String name;

    @Schema(description = "唯一标识（UE/业务ID）", example = "23")
    private String ue;

    @Schema(description = "是否选中", example = "false")
    private Boolean isCheck;
}
