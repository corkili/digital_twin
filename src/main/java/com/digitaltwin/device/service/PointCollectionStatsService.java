package com.digitaltwin.device.service;

import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.websocket.model.SensorData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointCollectionStatsService {
    
    private final PointRepository pointRepository;
    
    /**
     * 更新点位采集统计信息
     * 
     * @param sensorData 传感器数据
     */
    @Transactional
    public void updatePointCollectionStats(SensorData sensorData) {
        try {
            if (sensorData.getPointDataMap() == null || sensorData.getPointDataMap().isEmpty()) {
                return;
            }
            
            // 获取所有点位标识
            List<String> pointIdentities = sensorData.getPointDataMap().keySet().stream()
                    .collect(Collectors.toList());
            
            // 查询数据库中对应的点位
            List<Point> points = pointRepository.findByIdentityIn(pointIdentities);
            
            LocalDateTime now = LocalDateTime.now();
            
            // 更新每个点位的采集统计信息
            for (Point point : points) {
                // 更新最近采集时间
                point.setLastCollectionTime(now);
                
                // 如果是第一次采集，初始化总采集时长
                if (point.getTotalCollectionDuration() == null) {
                    point.setTotalCollectionDuration(0L);
                }
                
                // 如果有创建时间，计算从创建到现在的时长
                if (point.getCreatedAt() != null) {
                    Duration duration = Duration.between(point.getCreatedAt(), now);
                    point.setTotalCollectionDuration(duration.getSeconds());
                }
                
                // 更新总采集条数
                if (point.getTotalCollectionCount() == null) {
                    point.setTotalCollectionCount(1L);
                } else {
                    point.setTotalCollectionCount(point.getTotalCollectionCount() + 1);
                }
            }
            
            // 批量保存更新
            if (!points.isEmpty()) {
                pointRepository.saveAll(points);
            }
            
        } catch (Exception e) {
            log.error("更新点位采集统计信息时发生错误: {}", e.getMessage(), e);
        }
    }
}