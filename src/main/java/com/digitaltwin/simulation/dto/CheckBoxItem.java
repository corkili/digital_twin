package com.digitaltwin.simulation.dto;

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
public class CheckBoxItem {
    
    private String name;      // 复选框名称，如："氮气"
    private String ue;        // 唯一标识，如："23"
    private Boolean isCheck;  // 是否选中，如：false
}