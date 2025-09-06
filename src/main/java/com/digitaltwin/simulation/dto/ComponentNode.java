package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "组件节点，支持嵌套结构")
public class ComponentNode {
    
    @Schema(description = "组件名称", example = "主控制器", required = true)
    private String name;
    
    @Schema(description = "子组件列表，支持多层嵌套")
    private List<ComponentNode> children;
}