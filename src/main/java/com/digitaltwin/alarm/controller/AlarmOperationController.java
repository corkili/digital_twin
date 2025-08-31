package com.digitaltwin.alarm.controller;

import com.digitaltwin.alarm.service.AlarmOperationService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmOperationController {
    
    private final AlarmOperationService alarmOperationService;
    
    /**
     * 确认告警
     * 
     * @param alarmId 告警ID
     * @return 操作结果
     */
    @PostMapping("/{alarmId}/ack")
    public WebSocketResponse<String> ackAlarm(@PathVariable Long alarmId) {
        try {
            boolean success = alarmOperationService.acknowledgeAlarm(alarmId);
            if (success) {
                return WebSocketResponse.success("告警已确认");
            } else {
                return WebSocketResponse.error("告警确认失败");
            }
        } catch (Exception e) {
            log.error("确认告警时发生错误: {}", e.getMessage(), e);
            return WebSocketResponse.error("确认告警时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 忽略告警
     * 
     * @param alarmId 告警ID
     * @return 操作结果
     */
    @PostMapping("/{alarmId}/ignore")
    public WebSocketResponse<String> ignoreAlarm(@PathVariable Long alarmId) {
        try {
            boolean success = alarmOperationService.ignoreAlarm(alarmId);
            if (success) {
                return WebSocketResponse.success("告警已忽略");
            } else {
                return WebSocketResponse.error("告警忽略失败");
            }
        } catch (Exception e) {
            log.error("忽略告警时发生错误: {}", e.getMessage(), e);
            return WebSocketResponse.error("忽略告警时发生错误: " + e.getMessage());
        }
    }
}