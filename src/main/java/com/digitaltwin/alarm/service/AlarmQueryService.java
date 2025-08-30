package com.digitaltwin.alarm.service;

import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmState;
import com.digitaltwin.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmQueryService {

    private final AlarmRepository alarmRepository;

    /**
     * 根据设备ID获取告警
     * 
     * @param deviceId 设备ID
     * @return 告警列表
     */
    public List<Alarm> getAlarmsByDeviceId(Long deviceId) {
        try {
            return alarmRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
        } catch (Exception e) {
            log.error("根据设备ID获取告警失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取设备告警失败", e);
        }
    }

    /**
     * 根据点位标识获取告警
     * 
     * @param pointId 点位标识
     * @return 告警列表
     */
    public List<Alarm> getAlarmsByPointId(String pointId) {
        try {
            return alarmRepository.findByPointIdOrderByTimestampDesc(pointId);
        } catch (Exception e) {
            log.error("根据点位标识获取告警失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取点位告警失败", e);
        }
    }

    /**
     * 根据传感器ID获取告警
     * 
     * @param sensorId 传感器ID
     * @return 告警列表
     */
    public List<Alarm> getAlarmsBySensorId(String sensorId) {
        try {
            return alarmRepository.findBySensorIdOrderByTimestampDesc(sensorId);
        } catch (Exception e) {
            log.error("根据传感器ID获取告警失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取传感器告警失败", e);
        }
    }

    /**
     * 获取所有告警
     * 
     * @return 告警列表
     */
    public List<Alarm> getAllAlarms() {
        try {
            return alarmRepository.findAll();
        } catch (Exception e) {
            log.error("获取所有告警失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取所有告警失败", e);
        }
    }
    
    /**
     * 根据告警状态获取告警
     * 
     * @param state 告警状态
     * @return 告警列表
     */
    public List<Alarm> getAlarmsByState(AlarmState state) {
        try {
            return alarmRepository.findByStateOrderByTimestampDesc(state);
        } catch (Exception e) {
            log.error("根据告警状态获取告警失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取告警失败", e);
        }
    }
    
    /**
     * 根据告警状态和设备ID获取告警
     * 
     * @param state 告警状态
     * @param deviceId 设备ID
     * @return 告警列表
     */
    public List<Alarm> getAlarmsByStateAndDeviceId(AlarmState state, Long deviceId) {
        try {
            return alarmRepository.findByStateAndDeviceIdOrderByTimestampDesc(state, deviceId);
        } catch (Exception e) {
            log.error("根据告警状态和设备ID获取告警失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取告警失败", e);
        }
    }

    /**
     * 根据时间范围获取告警总次数
     * 
     * @param timeRange 时间范围（今日、本周、本月、全年）
     * @return 告警总次数
     */
    public Long getAlarmCountByTimeRange(String timeRange) {
        try {
            LocalDateTime[] timeRangeArray = getTimeRange(timeRange);
            return alarmRepository.countByTimeRange(timeRangeArray[0], timeRangeArray[1]);
        } catch (Exception e) {
            log.error("根据时间范围获取告警总次数失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取告警总次数失败", e);
        }
    }

    /**
     * 根据时间范围获取告警列表
     * 
     * @param timeRange 时间范围（今日、本周、本月、全年）
     * @param pageNum 页码（从1开始）
     * @param pageCount 每页数量
     * @return 告警列表
     */
    public List<Alarm> getAlarmsByTimeRange(String timeRange, int pageNum, int pageCount) {
        try {
            LocalDateTime[] timeRangeArray = getTimeRange(timeRange);
            // 创建分页对象
            Pageable pageable = PageRequest.of(pageNum - 1, pageCount, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 使用支持分页的仓库方法
            return alarmRepository.findByTimeRangeWithPagination(timeRangeArray[0], timeRangeArray[1], pageable);
        } catch (Exception e) {
            log.error("根据时间范围获取告警列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取告警列表失败", e);
        }
    }

    /**
     * 根据时间范围字符串获取开始和结束时间
     * 
     * @param timeRange 时间范围字符串
     * @return 包含开始时间和结束时间的数组
     */
    private LocalDateTime[] getTimeRange(String timeRange) {
        LocalDateTime startTime;
        LocalDateTime endTime = LocalDateTime.now();

        switch (timeRange) {
            case "今日":
                startTime = endTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "本周":
                startTime = endTime.minusDays(endTime.getDayOfWeek().getValue() - 1)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "本月":
                startTime = endTime.withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "全年":
                startTime = endTime.withDayOfYear(1)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            default:
                throw new IllegalArgumentException("不支持的时间范围: " + timeRange);
        }

        return new LocalDateTime[]{startTime, endTime};
    }
}