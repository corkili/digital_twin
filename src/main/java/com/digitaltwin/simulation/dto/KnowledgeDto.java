package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.Knowledge;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库详细信息")
public class KnowledgeDto {
    
    @Schema(description = "知识库ID", example = "1")
    private Long id;
    
    @Schema(description = "知识库标题", example = "数字孪生技术指南", required = true)
    private String title;
    
    @Schema(description = "目录结构数组")
    private List<CatalogItem> catalog;
    
    @Schema(description = "知识库状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    /**
     * 从实体类转换为DTO
     * @param knowledge 实体类
     * @return DTO
     */
    public static KnowledgeDto fromEntity(Knowledge knowledge) {
        if (knowledge == null) {
            return null;
        }
        
        KnowledgeDto dto = new KnowledgeDto();
        dto.setId(knowledge.getId());
        dto.setTitle(knowledge.getTitle());
        dto.setStatus(knowledge.getStatus());
        dto.setCreatedAt(knowledge.getCreatedAt());
        // catalog 需要在 Service 层解析 JSON 后设置
        
        return dto;
    }
}