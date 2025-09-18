package com.digitaltwin.trial.service;

import com.digitaltwin.trial.entity.Trial;
import com.digitaltwin.trial.repository.TrialRepository;
import com.digitaltwin.websocket.service.WebSocketPushService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.digitaltwin.trial.dto.TrailHistoryData;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.websocket.service.TDengineService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.Timestamp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrialService {

    private final TrialRepository trialRepository;
    private final WebSocketPushService webSocketPushService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
    private final TDengineService tdengineService;

    /**
     * 创建试验
     * 创建试验时必须传入开始时间、试验名称，可选传入试验编号、试验模式
     *
     * @param name 试验名称
     * @param startTimestamp 试验开始毫秒级时间戳
     * @param runNo 试验编号（可选）
     * @param mode 试验模式（可选）
     * @return 创建的试验对象
     */
    public Trial createTrial(String name, Long startTimestamp, String runNo, String mode) {
        Trial trial = new Trial(name, startTimestamp, runNo, mode);
        return trialRepository.save(trial);
    }

    /**
     * 创建试验（仅必须参数）
     *
     * @param name 试验名称
     * @param startTimestamp 试验开始毫秒级时间戳
     * @return 创建的试验对象
     */
    public Trial createTrial(String name, Long startTimestamp) {
        Trial trial = new Trial(name, startTimestamp);
        return trialRepository.save(trial);
    }

    /**
     * 更新试验
     * 更新试验时不允许更新开始时间和试验名称
     *
     * @param id 试验ID
     * @param runNo 试验编号
     * @param mode 试验模式
     * @param endTimestamp 试验结束毫秒级时间戳
     * @return 更新后的试验对象
     * @throws IllegalArgumentException 如果试验不存在
     */
    public Trial updateTrial(Long id, String runNo, String mode, Long endTimestamp) {
        Optional<Trial> optionalTrial = trialRepository.findById(id);
        if (optionalTrial.isPresent()) {
            Trial trial = optionalTrial.get();
            // 不允许更新开始时间和试验名称
            // trial.setName(name); // 禁止更新
            // trial.setStartTimestamp(startTimestamp); // 禁止更新
            
            // 可选更新字段
            if (runNo != null) {
                trial.setRunNo(runNo);
            }
            if (mode != null) {
                trial.setMode(mode);
            }
            if (endTimestamp != null) {
                trial.setEndTimestamp(endTimestamp);
            }
            
            return trialRepository.save(trial);
        } else {
            throw new IllegalArgumentException("Trial with id " + id + " not found");
        }
    }

    /**
     * 获取最后一个未结束的试验
     *
     * @return 最后一个未结束的试验对象，如果不存在则返回空Optional
     */
    public Optional<Trial> getLastUnfinishedTrial() {
        List<Trial> trials = trialRepository.findLastUnfinishedTrial();
        return trials.isEmpty() ? Optional.empty() : Optional.of(trials.get(0));
    }

    /**
     * 根据ID获取试验
     *
     * @param id 试验ID
     * @return 试验对象
     */
    public Optional<Trial> getTrialById(Long id) {
        return trialRepository.findById(id);
    }
    
    /**
     * 分页获取试验列表，支持通过试验名称、试验编号、试验日期做过滤
     *
     * @param name 试验名称（模糊匹配）
     * @param runNo 试验编号（精准匹配）
     * @param dateStr 试验日期（yyyyMMdd格式）
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 分页结果
     */
    public Page<Trial> getTrialsWithFilters(String name, String runNo, String dateStr, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // 忽略空参数
        if (name != null && name.isEmpty()) {
            name = null;
        }
        if (runNo != null && runNo.isEmpty()) {
            runNo = null;
        }
        if (dateStr != null && dateStr.isEmpty()) {
            dateStr = null;
        }

        Long startTimestamp = null;
        Long endTimestamp = null;

        if (dateStr != null) {
            LocalDate targetDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            // 获取日期对应0点0时0分0秒的毫秒级时间戳
            startTimestamp = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            // 获取日期对应23点59分59秒的毫秒级时间戳
            endTimestamp = targetDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        
        // 先使用名称和编号过滤
        return trialRepository.findByNameContainingIgnoreCaseAndRunNo(name, runNo, startTimestamp, endTimestamp, pageable);
    }
    
    /**
     * 获取试验总数
     *
     * @return 试验总数
     */
    public long getCount() {
        return trialRepository.count();
    }

    public void pushTrailHistoryData(Trial trial, List<Point> points, String uniqueId) {
        // 遍历所有的Point，并使用TDengineService的querySensorDataByTimeRangeAndPointKey方法查询点位数据
        Map<Long, Map<String, String>> dataMap = new HashMap<>();
        
        long now = System.currentTimeMillis();
        long startTime = trial.getStartTimestamp();
        long endTime = trial.getEndTimestamp() != null ? trial.getEndTimestamp() : now;
        
        try {
            // 存储所有CompletableFuture的列表
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            // 遍历所有的Point
            for (Point point : points) {
                // 注意for循环变量逃逸问题，创建final变量
                final String pointIdentity = point.getIdentity();

                // 为每个点位的时间区间创建一个CompletableFuture
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    List<Map<String, Object>> pointDataList = tdengineService.querySensorDataByTimeRangeAndPointKey(
                        startTime,
                        endTime,
                        pointIdentity
                    );

                    // 同步处理dataMap，注意并发安全问题
                    synchronized (dataMap) {
                        for (Map<String, Object> pointData : pointDataList) {
                            Timestamp ts = (Timestamp) pointData.get("ts");
                            Long timestamp = ts.getTime();
                            String value = (String) pointData.get("point_value");

                            dataMap.computeIfAbsent(timestamp, k -> new HashMap<>()).put(pointIdentity, value);
                        }
                    }
                }, executorService);

                futures.add(future);
            }
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
        } catch (Exception e) {
            log.error("查询点位数据时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("查询点位数据时发生错误: " + e.getMessage(), e);
        }
        
        // 整合数据：将所有数据整合到一个List<TrailHistoryData>中
        List<TrailHistoryData> result = new ArrayList<>();
        for (Map.Entry<Long, Map<String, String>> entry : dataMap.entrySet()) {
            result.add(new TrailHistoryData(entry.getKey(), entry.getValue(), uniqueId));
        }
        
        // 按时间戳升序排列
        result.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        
        // 通过WebSocket推送历史数据
        try {
            // 逐条推送数据，每条数据间隔N毫秒，N为下一条数据的时间戳减去当前数据的时间戳
            for (int i = 0; i < result.size(); i++) {
                TrailHistoryData currentData = result.get(i);
                
                // 推送当前数据
                webSocketPushService.pushHistoryDataToSubscribers(WebSocketResponse.success(currentData));
                
                // 如果不是最后一条数据，计算间隔时间并等待
                if (i < result.size() - 1) {
                    TrailHistoryData nextData = result.get(i + 1);
                    long interval = nextData.getTimestamp() - currentData.getTimestamp();
                    
                    // 确保间隔时间不为负数
                    if (interval > 0) {
                        Thread.sleep(interval);
                    }
                }
            }
            
            // 推送完成标志：发送timestamp为-1的数据
            TrailHistoryData completionData = new TrailHistoryData();
            completionData.setTimestamp(-1L);
            completionData.setSubscribeId(uniqueId);
            webSocketPushService.pushHistoryDataToSubscribers(WebSocketResponse.success(completionData));
        } catch (InterruptedException e) {
            log.error("推送历史数据时被中断: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("推送历史数据时发生错误: {}", e.getMessage(), e);
        }
        

    }

    public List<TrailHistoryData> getTrailHistoryData(Trial trial, List<Point> points) {
        // 3. 遍历所有的Point，并使用TDengineService的querySensorDataByTimeRangeAndPointKey方法查询点位数据
        Map<Long, Map<String, String>> dataMap = new HashMap<>();
        
        long now = System.currentTimeMillis();
        long startTime = trial.getStartTimestamp();
        long endTime = trial.getEndTimestamp() != null ? trial.getEndTimestamp() : now;
        
        try {
            // 存储所有CompletableFuture的列表
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            // 遍历所有的Point
            for (Point point : points) {
                // 注意for循环变量逃逸问题，创建final变量
                final String pointIdentity = point.getIdentity();

                // 为每个点位的时间区间创建一个CompletableFuture
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    List<Map<String, Object>> pointDataList = tdengineService.querySensorDataByTimeRangeAndPointKey(
                        startTime,
                        endTime,
                        pointIdentity
                    );

                    // 同步处理dataMap，注意并发安全问题
                    synchronized (dataMap) {
                        for (Map<String, Object> pointData : pointDataList) {
                            Timestamp ts = (Timestamp) pointData.get("ts");
                            Long timestamp = ts.getTime();
                            String value = (String) pointData.get("point_value");

                            dataMap.computeIfAbsent(timestamp, k -> new HashMap<>()).put(pointIdentity, value);
                        }
                    }
                }, executorService);

                futures.add(future);
            }
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
        } catch (Exception e) {
            log.error("查询点位数据时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("查询点位数据时发生错误: " + e.getMessage(), e);
        }
        
        // 4. 整合数据：将所有数据整合到一个List<TrailHistoryData>中
        List<TrailHistoryData> result = new ArrayList<>();
        for (Map.Entry<Long, Map<String, String>> entry : dataMap.entrySet()) {
            result.add(new TrailHistoryData(entry.getKey(), entry.getValue()));
        }
        
        // 5. 按时间戳升序排列
        result.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        
        return result;
    }
}