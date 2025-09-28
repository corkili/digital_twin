package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.PointFailureRecordDto;
import com.digitaltwin.device.dto.device.FailureStatisticsDto;
import com.digitaltwin.device.service.PointFailureRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/point-failures")
@RequiredArgsConstructor
@Tag(name = "点位故障记录管理", description = "提供点位故障记录的创建、查询和统计接口")
public class PointFailureRecordController {
    
    private final PointFailureRecordService pointFailureRecordService;
    
    /**
     * 记录点位故障信息
     */
    @Operation(summary = "记录点位故障", description = "创建新的点位故障记录，记录故障详细信息")
    @PostMapping
    public ResponseEntity<ApiResponse> recordFailure(
            @Parameter(description = "故障记录请求信息", required = true) @RequestBody PointFailureRecordRequest request) {
        try {
            PointFailureRecordDto record = pointFailureRecordService.recordFailure(
                    request.getPointId(), 
                    request.getDescription(),"test");
            return ResponseEntity.ok(ApiResponse.success("故障记录创建成功", record));
        } catch (Exception e) {
            log.error("创建故障记录失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("创建故障记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最近7天每天的故障数量统计
     */
    @Operation(summary = "获取最近7天故障统计", description = "统计最近7天每天的点位故障数量，用于趋势分析")
    @GetMapping("/statistics/last-7-days")
    public ResponseEntity<ApiResponse> getFailureStatisticsForLast7Days() {
        try {
            List<FailureStatisticsDto> statistics = pointFailureRecordService.getFailureStatisticsForLast7Days();
            return ResponseEntity.ok(ApiResponse.success("查询成功", statistics));
        } catch (Exception e) {
            log.error("查询故障统计失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("查询故障统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 分页获取所有故障记录
     */
    @Operation(summary = "分页查询故障记录", description = "分页查询所有点位故障记录，支持排序参数")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllFailureRecords(
            @Parameter(description = "页码（从0开始）", required = false) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", required = false) @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段", required = false) @RequestParam(defaultValue = "failureTime") String sortBy,
            @Parameter(description = "排序方向（asc/desc）", required = false) @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<PointFailureRecordDto> records = pointFailureRecordService.getAllFailureRecords(pageable);
            return ResponseEntity.ok(ApiResponse.success("查询成功", records));
        } catch (Exception e) {
            log.error("查询故障记录失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("查询故障记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 点位故障记录请求类
     */
    public static class PointFailureRecordRequest {
        private Long pointId;
        private String description;
        
        // Getters and Setters
        public Long getPointId() {
            return pointId;
        }
        
        public void setPointId(Long pointId) {
            this.pointId = pointId;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}