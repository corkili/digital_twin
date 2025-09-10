package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 创建知识库请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建知识库请求")
public class CreateKnowledgeRequest {
    
    @Schema(description = "知识库标题", example = "数字孪生技术基础教程", required = true)
    private String title;
    
    @Schema(description = "目录结构数组", required = true, example = "[{\"name\": \"传感器配置\", \"ue\": \"ue_sensor_001\", \"filePath\": \"/docs/sensor-config.pdf\"}, {\"name\": \"数据采集\", \"ue\": \"ue_data_002\", \"filePath\": \"/docs/data-collection.pdf\"}]")
    private List<CatalogItem> catalog;
}