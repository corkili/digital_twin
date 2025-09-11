package com.digitaltwin.device.service;

import com.digitaltwin.device.entity.PointFailureRecord;
import com.digitaltwin.device.repository.PointFailureRecordRepository;
import com.digitaltwin.device.dto.device.PointFailureRecordDto;
import com.digitaltwin.device.dto.device.FailureStatisticsDto;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
    public PointFailureRecordDto recordFailure(Long pointId, String description,String value) {
        try {
            // 检查点位是否存在
            Point point = pointRepository.findById(pointId)
                    .orElseThrow(() -> new RuntimeException("点位不存在，ID: " + pointId));
            
            PointFailureRecord record = new PointFailureRecord();
            record.setPointId(pointId);
            record.setDescription(description);
            // 设置记录创建时间
            LocalDateTime now = LocalDateTime.now();
            record.setFailureTime(now);
            // 设置故障开始时间
            record.setStartTime(now);
            record.setFailureValue(value);
            // 初始状态为NEW
            record.setStatus("NEW");
            
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
     * 获取指定点位的活动故障记录（未解决的记录）
     * @param pointId 点位ID
     * @return 活动故障记录，如果没有则返回null
     */
    public PointFailureRecord getActiveFailureRecord(Long pointId) {
        try {
            List<PointFailureRecord> records = pointFailureRecordRepository.findByPointIdAndStatusOrderByStartTimeDesc(pointId, "NEW");
            return records.isEmpty() ? null : records.get(0); // 返回最新的活动记录
        } catch (Exception e) {
            log.error("获取活动故障记录失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 解决故障记录，设置结束时间和持续时间
     * @param recordId 记录ID
     * @param description 解决描述
     * @return 更新后的故障记录DTO
     */
    public PointFailureRecordDto resolveFailure(Long recordId, String description) {
        try {
            PointFailureRecord record = pointFailureRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("故障记录不存在，ID: " + recordId));
            
            // 设置结束时间
            LocalDateTime endTime = LocalDateTime.now();
            record.setEndTime(endTime);
            
            // 计算持续时间（秒）
            long duration = ChronoUnit.SECONDS.between(record.getStartTime(), endTime);
            record.setDuration(duration);
            
            // 更新状态为已解决
            record.setStatus("RESOLVED");
            
            // 更新描述
            record.setDescription(record.getDescription() + "; " + description);
            
            PointFailureRecord savedRecord = pointFailureRecordRepository.save(record);
            
            // 构造返回的DTO对象
            PointFailureRecordDto dto = new PointFailureRecordDto(savedRecord);
            
            // 通过pointId查询Point实体获取pointIdentity和pointName
            pointRepository.findById(record.getPointId()).ifPresent(point -> {
                dto.setPointIdentity(point.getIdentity());
                dto.setPointName(point.getDevice() != null ? 
                    point.getDevice().getName() + "." + point.getIdentity() : 
                    point.getIdentity());
                
                // 设置设备名和通道名
                if (point.getDevice() != null) {
                    Device device = point.getDevice();
                    dto.setDeviceName(device.getName());
                    
                    if (device.getChannel() != null) {
                        Channel channel = device.getChannel();
                        dto.setChannelName(channel.getName());
                    }
                }
            });
            
            log.info("更新故障记录成功: 记录ID={}, 描述={}", recordId, description);
            return dto;
        } catch (Exception e) {
            log.error("更新故障记录失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新故障记录失败: " + e.getMessage());
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
     * 获取所有故障记录（分页）
     * @param pageable 分页参数
     * @return 故障记录分页列表
     */
    public Page<PointFailureRecordDto> getAllFailureRecords(Pageable pageable) {
        Page<PointFailureRecord> records = pointFailureRecordRepository.findAll(pageable);
        List<PointFailureRecordDto> dtos = records.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, records.getTotalElements());
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
            
            // 设置设备名和通道名
            if (point.getDevice() != null) {
                Device device = point.getDevice();
                dto.setDeviceName(device.getName());
                
                if (device.getChannel() != null) {
                    Channel channel = device.getChannel();
                    dto.setChannelName(channel.getName());
                }
            }
        });
        
        return dto;
    }
}