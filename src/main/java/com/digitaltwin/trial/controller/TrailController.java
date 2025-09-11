package com.digitaltwin.trial.controller;

import com.digitaltwin.trial.dto.TrailListResponse;
import com.digitaltwin.trial.dto.TrialDto;
import com.digitaltwin.trial.entity.Trial;
import com.digitaltwin.trial.service.TrialService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/trial")
@RequiredArgsConstructor
@Tag(name = "试验管理", description = "提供试验的分页查询")
public class TrailController {

    private final TrialService trialService;

    /**
     * 获取试验列表（支持分页和过滤）
     *
     * @param name 试验名称（模糊匹配）
     * @param runNo 试验编号（精准匹配）
     * @param date 试验日期（yyyyMMdd格式）
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param sortBy 排序字段（默认为id）
     * @param sortDir 排序方向（默认为DESC）
     * @return 试验列表
     */
    @Operation(summary = "获取试验列表（支持分页和过滤）")
    @GetMapping("/list")
    public WebSocketResponse<TrailListResponse> list(
            @Parameter(description = "试验名称（模糊匹配）") @RequestParam(required = false) String name,
            @Parameter(description = "试验编号（精准匹配）") @RequestParam(required = false) String runNo,
            @Parameter(description = "试验日期（yyyyMMdd格式）") @RequestParam(required = false) String date,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String sortDir) {
        try {
            // 调用服务层方法获取分页数据
            Page<Trial> trialPage = trialService.getTrialsWithFilters(name, runNo, date, page, size, sortBy, sortDir);
            
            // 转换为TrailListResponse
            TrailListResponse response = new TrailListResponse(
                trialPage.getTotalElements(),
                trialPage.getContent().stream()
                    .map(TrailListResponse.TrailListItem::new)
                    .collect(Collectors.toList())
            );
            
            return WebSocketResponse.success(response);
        } catch (Exception e) {
            log.error("获取试验列表失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取试验列表失败: " + e.getMessage());
        }
    }
}