package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 目录项DTO
 * 包含目录名称、对应的UE ID和文件路径
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库目录项")
public class CatalogItem {
    
    @Schema(description = "目录名称", example = "传感器配置", required = true)
    private String name;
    
    @Schema(description = "UE引擎ID", example = "ue_sensor_001", required = true)
    private String ue;
    
    @Schema(description = "文件路径", example = "/docs/sensor-config.pdf", required = true)
    private String filePath;
}