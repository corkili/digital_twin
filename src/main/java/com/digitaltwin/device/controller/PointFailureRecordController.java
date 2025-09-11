package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.PointFailureRecordDto;
import com.digitaltwin.device.dto.device.FailureStatisticsDto;
import com.digitaltwin.device.service.PointFailureRecordService;
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
public class PointFailureRecordController {
    
    private final PointFailureRecordService pointFailureRecordService;
    
    /**
     * 记录点位故障信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse> recordFailure(@RequestBody PointFailureRecordRequest request) {
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
    @GetMapping
    public ResponseEntity<ApiResponse> getAllFailureRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "failureTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
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