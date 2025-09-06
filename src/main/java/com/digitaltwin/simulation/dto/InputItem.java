package com.digitaltwin.simulation.dto;

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
public class InputItem {
    
    private String prefix1;   // 前缀1，如："打开:"
    private String value1;    // 值1，如：""
    private String prefix2;   // 前缀2，如："转速"
    private String value2;    // 值2，如：""
    private String button;    // 按钮文本，如："启动"
}