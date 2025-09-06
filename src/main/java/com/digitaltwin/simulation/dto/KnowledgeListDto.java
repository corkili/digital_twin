package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.Knowledge;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库列表DTO
 * 用于列表查询，不包含详细的目录信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeListDto {
    
    private Long id;                 // 知识库ID
    private String title;            // 标题
    private String status;           // 状态
    private LocalDateTime createdAt; // 创建时间
    
    /**
     * 从实体类转换为列表DTO
     * @param knowledge 实体类
     * @return 列表DTO
     */
    public static KnowledgeListDto fromEntity(Knowledge knowledge) {
        if (knowledge == null) {
            return null;
        }
        
        KnowledgeListDto dto = new KnowledgeListDto();
        dto.setId(knowledge.getId());
        dto.setTitle(knowledge.getTitle());
        dto.setStatus(knowledge.getStatus());
        dto.setCreatedAt(knowledge.getCreatedAt());
        
        return dto;
    }
}