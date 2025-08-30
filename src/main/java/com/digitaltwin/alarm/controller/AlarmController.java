package com.digitaltwin.alarm.controller;

import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmState;
import com.digitaltwin.alarm.service.AlarmQueryService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmQueryService alarmQueryService;

    /**
     * 根据设备ID获取告警
     * 
     * @param deviceId 设备ID
     * @return 告警列表
     */
    @GetMapping("/device/{deviceId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByDeviceId(@PathVariable Long deviceId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByDeviceId(deviceId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取设备告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取设备告警失败: " + e.getMessage());
        }
    }

    /**
     * 根据点位标识获取告警
     * 
     * @param pointId 点位标识
     * @return 告警列表
     */
    @GetMapping("/point/{pointId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByPointId(@PathVariable String pointId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByPointId(pointId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取点位告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取点位告警失败: " + e.getMessage());
        }
    }

    /**
     * 根据传感器ID获取告警
     * 
     * @param sensorId 传感器ID
     * @return 告警列表
     */
    @GetMapping("/sensor/{sensorId}")
    public WebSocketResponse<List<Alarm>> getAlarmsBySensorId(@PathVariable String sensorId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsBySensorId(sensorId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取传感器告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取传感器告警失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有告警
     * 
     * @return 告警列表
     */
    @GetMapping("/all")
    public WebSocketResponse<List<Alarm>> getAllAlarms() {
        try {
            List<Alarm> alarms = alarmQueryService.getAllAlarms();
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取所有告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取所有告警失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据告警状态获取告警
     * 
     * @param state 告警状态
     * @return 告警列表
     */
    @GetMapping("/state/{state}")
    public WebSocketResponse<List<Alarm>> getAlarmsByState(@PathVariable AlarmState state) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByState(state);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("根据告警状态获取告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取告警失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据告警状态和设备ID获取告警
     * 
     * @param state 告警状态
     * @param deviceId 设备ID
     * @return 告警列表
     */
    @GetMapping("/state/{state}/device/{deviceId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByStateAndDeviceId(
            @PathVariable AlarmState state, 
            @PathVariable Long deviceId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByStateAndDeviceId(state, deviceId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("根据告警状态和设备ID获取告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取告警失败: " + e.getMessage());
        }
    }
}