package com.digitaltwin.device.service;

import com.digitaltwin.device.entity.PointFailureRecord;
import com.digitaltwin.device.repository.PointFailureRecordRepository;
import com.digitaltwin.device.dto.device.PointFailureRecordDto;
import com.digitaltwin.device.dto.device.FailureStatisticsDto;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointFailureRecordService {
    
    private final PointFailureRecordRepository pointFailureRecordRepository;
    private final PointRepository pointRepository;
    
    /**
     * 记录点位故障信息
     * @param pointId 点位ID
     * @param description 故障描述
     * @return 故障记录DTO
     */
    public PointFailureRecordDto recordFailure(Long pointId, String description) {
        try {
            // 检查点位是否存在
            Point point = pointRepository.findById(pointId)
                    .orElseThrow(() -> new RuntimeException("点位不存在，ID: " + pointId));
            
            PointFailureRecord record = new PointFailureRecord();
            record.setPointId(pointId);
            record.setDescription(description);
            record.setFailureTime(LocalDateTime.now());
            
            PointFailureRecord savedRecord = pointFailureRecordRepository.save(record);
            
            // 构造返回的DTO对象
            PointFailureRecordDto dto = new PointFailureRecordDto(savedRecord);
            dto.setPointIdentity(point.getIdentity());
            dto.setPointName(point.getDevice() != null ? 
                point.getDevice().getName() + "." + point.getIdentity() : 
                point.getIdentity());
            
            log.info("记录点位故障信息成功: 点位={}, 描述={}", point.getIdentity(), description);
            return dto;
        } catch (Exception e) {
            log.error("记录点位故障信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("记录点位故障信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最近7天每天的故障数量统计
     * @return 故障统计列表
     */
    public List<FailureStatisticsDto> getFailureStatisticsForLast7Days() {
        List<FailureStatisticsDto> statistics = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // 获取最近7天的数据（包括今天）
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.atTime(LocalTime.MAX);
            
            long count = pointFailureRecordRepository.countByFailureTimeBetween(startTime, endTime);
            statistics.add(new FailureStatisticsDto(date, count));
        }
        
        return statistics;
    }
    
    /**
     * 获取所有故障记录
     * @return 故障记录列表
     */
    public List<PointFailureRecordDto> getAllFailureRecords() {
        return pointFailureRecordRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 将PointFailureRecord实体转换为PointFailureRecordDto
     * @param record PointFailureRecord实体
     * @return PointFailureRecordDto对象
     */
    private PointFailureRecordDto convertToDto(PointFailureRecord record) {
        PointFailureRecordDto dto = new PointFailureRecordDto(record);
        
        // 通过pointId查询Point实体获取pointIdentity和pointName
        pointRepository.findById(record.getPointId()).ifPresent(point -> {
            dto.setPointIdentity(point.getIdentity());
            dto.setPointName(point.getDevice() != null ? 
                point.getDevice().getName() + "." + point.getIdentity() : 
                point.getIdentity());
        });
        
        return dto;
    }
}