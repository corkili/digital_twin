package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "虚拟场景详细信息")
public class VirtualSceneDetailDto {
    
    @Schema(description = "场景ID", example = "1")
    private Long id;
    
    @Schema(description = "场景名称", example = "工厂仿真场景")
    private String name;
    
    @Schema(description = "详情内容，包含图片链接", example = "这是一个工厂仿真场景，包含多个生产线和设备...")
    private String content;
    
    @Schema(description = "组件结构树")
    private List<ComponentNode> components;
    
    @Schema(description = "包含的场景名称列表", example = "[\"生产线1\", \"仓储区\", \"质检区\"]")
    private List<String> scenes;
}