package com.digitaltwin.trial.controller;

import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.service.PointService;
import com.digitaltwin.trial.dto.TrailCountResponse;
import com.digitaltwin.trial.dto.TrailHistoryData;
import com.digitaltwin.trial.dto.TrailListResponse;
import com.digitaltwin.trial.entity.Trial;
import com.digitaltwin.trial.service.TrialService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import com.digitaltwin.websocket.service.TDengineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/trial")
@RequiredArgsConstructor
@Tag(name = "试验管理", description = "提供试验的分页查询")
public class TrailController {

    private final TrialService trialService;
    private final PointService pointService;
    private final TDengineService tdengineService;
    // 创建对象级别的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

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
            org.springframework.data.domain.Page<Trial> trialPage = trialService.getTrialsWithFilters(name, runNo, date, page, size, sortBy, sortDir);
            
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
    
    /**
     * 获取试验总数
     *
     * @return 试验总数
     */
    @Operation(summary = "获取试验总数")
    @GetMapping("/count")
    public WebSocketResponse<TrailCountResponse> count() {
        try {
            long count = trialService.getCount();
            return WebSocketResponse.success(new TrailCountResponse(count));
        } catch (Exception e) {
            log.error("获取试验总数失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取试验总数失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据试验ID查询所有点位数据
     *
     * @param id 试验ID
     * @return 试验的所有点位数据
     */
    @Operation(summary = "根据试验ID查询所有点位数据")
    @GetMapping("/{id}/history_data")
    public WebSocketResponse<List<TrailHistoryData>> getTrailHistoryDataById(
            @Parameter(description = "试验ID") @PathVariable Long id) {
        try {
            // 1. 从数据库中查询出对应的Trial
            Trial trial = trialService.getTrialById(id)
                    .orElseThrow(() -> new RuntimeException("Trial not found with id: " + id));
            
            // 2. 从数据库中查询出所有的Point
            List<Point> points = pointService.getAllPoints().stream()
                    .map(dto -> {
                        Point point = new Point();
                        point.setId(dto.getId());
                        point.setIdentity(dto.getIdentity());
                        return point;
                    })
                    .collect(Collectors.toList());
            
            // 3. 调用TrialService处理数据查询和整合逻辑
            List<TrailHistoryData> result = trialService.getTrailHistoryData(trial, points);
            
            return WebSocketResponse.success(result);
        } catch (Exception e) {
            log.error("获取试验历史数据失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取试验历史数据失败: " + e.getMessage());
        }
    }

    /**
     * 根据试验ID查询所有点位数据
     *
     * @param id 试验ID
     * @return 试验的所有点位数据
     */
    @Operation(summary = "根据试验ID查询所有点位数据")
    @PostMapping("/{id}/history_data")
    public WebSocketResponse<String> subscribeTrailHistoryDataById(
            @Parameter(description = "试验ID") @PathVariable Long id) {
        try {
            // 1. 从数据库中查询出对应的Trial
            Trial trial = trialService.getTrialById(id)
                    .orElseThrow(() -> new RuntimeException("Trial not found with id: " + id));
            
            // 2. 从数据库中查询出所有的Point
            List<Point> points = pointService.getAllPoints().stream()
                    .map(dto -> {
                        Point point = new Point();
                        point.setId(dto.getId());
                        point.setIdentity(dto.getIdentity());
                        return point;
                    })
                    .collect(Collectors.toList());
            
            // 3. 生成唯一字符串：当前时间戳+trail.id
            String uniqueId = System.currentTimeMillis() + "-" + trial.getId();
            
            // 4. 异步调用TrialService处理数据查询和整合逻辑
            executorService.submit(() -> {
                try {
                    trialService.pushTrailHistoryData(trial, points, uniqueId);
                } catch (Exception e) {
                    log.error("异步处理试验历史数据失败: {}", e.getMessage(), e);
                }
            });
            
            // 5. 返回成功响应，包含唯一字符串
            return WebSocketResponse.success(uniqueId);
        } catch (Exception e) {
            log.error("获取试验历史数据失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取试验历史数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 将时间范围拆分为多个不重叠的区间，每个区间最多包含interval个时间单位
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 区间长度
     * @return 时间区间列表
     */
    private List<TimeRange> splitTimeRange(long startTime, long endTime, long interval) {
        List<TimeRange> ranges = new ArrayList<>();
        long currentStart = startTime;
        
        while (currentStart <= endTime) {
            long currentEnd = Math.min(currentStart + interval - 1, endTime);
            ranges.add(new TimeRange(currentStart, currentEnd));
            currentStart = currentEnd + 1;
        }
        
        return ranges;
    }
    
    /**
     * 时间区间类
     */
    private static class TimeRange {
        private final long startTime;
        private final long endTime;
        
        public TimeRange(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public long getEndTime() {
            return endTime;
        }
    }
}