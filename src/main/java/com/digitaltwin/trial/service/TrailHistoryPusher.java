package com.digitaltwin.trial.service;

import com.digitaltwin.trial.entity.Trial;
import com.digitaltwin.websocket.service.WebSocketPushService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import com.digitaltwin.trial.service.TrailHistoryCacheService;
import com.digitaltwin.websocket.service.TDengineService;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.trial.dto.TrailHistoryData;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrailHistoryPusher {
    
    private final String uniqueId;
    private volatile double pushRate = 1.0; // 默认推送倍率为1
    private final Trial trial;
    private final List<Point> points;
    private final TrailHistoryCacheService trailHistoryCacheService;
    private final WebSocketPushService webSocketPushService;
    private final TDengineService tdengineService;
    private final ExecutorService executorService;
    
    public TrailHistoryPusher(String uniqueId, Trial trial, List<Point> points, 
                              TrailHistoryCacheService trailHistoryCacheService,
                              WebSocketPushService webSocketPushService,
                              TDengineService tdengineService,
                              ExecutorService executorService) {
        this.uniqueId = uniqueId;
        this.trial = trial;
        this.points = points;
        this.trailHistoryCacheService = trailHistoryCacheService;
        this.webSocketPushService = webSocketPushService;
        this.tdengineService = tdengineService;
        this.executorService = executorService;
    }
    
    public void push() {
        try {
            // 首先尝试从缓存中获取数据
            List<TrailHistoryData> result = trailHistoryCacheService.getIfPresent(trial.getId());
            
            // 如果缓存中没有数据，则查询数据库
            if (result == null) {
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
                result = new ArrayList<>();
                for (Map.Entry<Long, Map<String, String>> entry : dataMap.entrySet()) {
                    result.add(new TrailHistoryData(entry.getKey(), entry.getValue()));
                }
                
                // 按时间戳升序排列
                result.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
                
                // 将结果放入缓存，不包含uniqueId
                trailHistoryCacheService.put(trial.getId(), result);
            }
            
            // 创建包含uniqueId的新列表用于推送
            List<TrailHistoryData> resultWithUniqueId = result.stream()
                    .map(item -> {
                        TrailHistoryData itemWithId = new TrailHistoryData();
                        itemWithId.setTimestamp(item.getTimestamp());
                        itemWithId.setPointsData(item.getPointsData());
                        itemWithId.setSubscribeId(uniqueId);
                        return itemWithId;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            // 添加完成标志数据
            TrailHistoryData completionData = new TrailHistoryData();
            completionData.setTimestamp(-1L);
            completionData.setSubscribeId(uniqueId);
            resultWithUniqueId.add(completionData);
            
            // 通过WebSocket推送历史数据
            try {
                // 逐条推送数据，每条数据间隔N毫秒，N为下一条数据的时间戳减去当前数据的时间戳
                for (int i = 0; i < resultWithUniqueId.size() - 1; i++) { // 注意：最后一个元素是完成标志，不需要推送间隔
                    TrailHistoryData currentData = resultWithUniqueId.get(i);
                    
                    // 推送当前数据
                    webSocketPushService.pushHistoryDataToSubscribers(WebSocketResponse.success(currentData));
                    
                    // 如果不是最后一条数据，计算间隔时间并等待
                    if (i < resultWithUniqueId.size() - 2) { // 注意：倒数第二个元素才需要计算间隔
                        TrailHistoryData nextData = resultWithUniqueId.get(i + 1);
                        long interval = nextData.getTimestamp() - currentData.getTimestamp();
                        
                        // 确保间隔时间不为负数，并根据推送倍率调整间隔
                        if (interval > 0) {
                            long adjustedInterval = (long) (interval / getPushRate());
                            if (adjustedInterval > 0) {
                                Thread.sleep(adjustedInterval);
                            }
                        }
                    }
                }
                
                // 推送完成标志：发送timestamp为-1的数据
                webSocketPushService.pushHistoryDataToSubscribers(WebSocketResponse.success(completionData));
            } catch (InterruptedException e) {
                log.error("推送历史数据时被中断: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("推送历史数据时发生错误: {}", e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("推送历史数据时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // 设置推送倍率的方法，保证并发安全
    public synchronized void setRate(double rate) {
        this.pushRate = rate;
    }
    
    // 获取推送倍率的方法
    public synchronized double getRate() {
        if (this.pushRate <= 0) {
            return 1.0;
        }
        return this.pushRate;
    }
    
    // 获取推送倍率的方法，以便在TrialService中可以访问它
    public synchronized double getPushRate() {
        return this.pushRate;
    }
    
    // 获取唯一ID的方法
    public String getUniqueId() {
        return this.uniqueId;
    }
}