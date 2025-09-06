package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 知识库列表响应DTO
 * 统一处理列表和分页两种情况
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库列表响应")
public class KnowledgeListResponse {
    
    @Schema(description = "知识库列表数据")
    private List<KnowledgeDto> content;
    
    @Schema(description = "是否为分页数据", example = "true")
    private boolean paginated;
    
    @Schema(description = "当前页码（分页时有效）", example = "0")
    private Integer pageNumber;
    
    @Schema(description = "每页数量（分页时有效）", example = "10") 
    private Integer pageSize;
    
    @Schema(description = "总元素数（分页时有效）", example = "50")
    private Long totalElements;
    
    @Schema(description = "总页数（分页时有效）", example = "5")
    private Integer totalPages;
    
    @Schema(description = "是否为最后一页（分页时有效）", example = "false")
    private Boolean last;
    
    @Schema(description = "是否为第一页（分页时有效）", example = "true") 
    private Boolean first;
    
    /**
     * 从列表创建响应
     */
    public static KnowledgeListResponse fromList(List<KnowledgeDto> list) {
        KnowledgeListResponse response = new KnowledgeListResponse();
        response.setContent(list);
        response.setPaginated(false);
        return response;
    }
    
    /**
     * 从分页对象创建响应
     */
    public static KnowledgeListResponse fromPage(Page<KnowledgeDto> page) {
        KnowledgeListResponse response = new KnowledgeListResponse();
        response.setContent(page.getContent());
        response.setPaginated(true);
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        response.setFirst(page.isFirst());
        return response;
    }
}