package com.digitaltwin.simulation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 组件节点DTO
 * 支持嵌套结构的组件树
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentNode {
    
    private String name;                    // 组件名称
    private List<ComponentNode> children;   // 子组件列表
}