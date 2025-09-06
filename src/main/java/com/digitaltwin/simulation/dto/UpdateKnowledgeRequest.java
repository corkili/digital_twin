package com.digitaltwin.simulation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 更新知识库请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKnowledgeRequest {
    
    private String title;                 // 标题
    private List<CatalogItem> catalog;    // 目录数组
    private String status;                // 状态
}