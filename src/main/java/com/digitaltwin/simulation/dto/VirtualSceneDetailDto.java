package com.digitaltwin.simulation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 虚拟场景详情DTO
 * 包含场景详情和组件结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualSceneDetailDto {
    
    private Long id;                        // 场景ID
    private String name;                    // 场景名称
    private String content;                 // 详情内容（包含图片链接）
    private List<ComponentNode> components; // 组件结构
    private List<String> scenes;            // 场景名称数组
}