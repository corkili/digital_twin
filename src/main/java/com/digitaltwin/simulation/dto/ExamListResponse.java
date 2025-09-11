package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评定考核列表响应")
public class ExamListResponse {
    @Schema(description = "列表内容")
    private List<ExamDto> content;

    @Schema(description = "是否分页")
    private boolean paginated;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;

    @Schema(description = "总元素")
    private Long totalElements;

    @Schema(description = "总页数")
    private Integer totalPages;

    @Schema(description = "是否最后一页")
    private Boolean last;

    @Schema(description = "是否第一页")
    private Boolean first;

    public static ExamListResponse fromList(List<ExamDto> list) {
        ExamListResponse r = new ExamListResponse();
        r.setContent(list);
        r.setPaginated(false);
        return r;
    }

    public static ExamListResponse fromPage(Page<ExamDto> page) {
        ExamListResponse r = new ExamListResponse();
        r.setContent(page.getContent());
        r.setPaginated(true);
        r.setPage(page.getNumber());
        r.setSize(page.getSize());
        r.setTotalElements(page.getTotalElements());
        r.setTotalPages(page.getTotalPages());
        r.setLast(page.isLast());
        r.setFirst(page.isFirst());
        return r;
    }
}

