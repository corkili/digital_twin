package com.digitaltwin.alarm.controller;

import com.digitaltwin.alarm.service.AlarmOperationService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/alarms")
@RequiredArgsConstructor
@Tag(name = "告警操作管理", description = "提供告警操作接口，支持告警的确认和忽略操作")
public class AlarmOperationController {
    
    private final AlarmOperationService alarmOperationService;
    
    /**
     * 确认告警
     *
     * @param alarmId 告警ID
     * @return 操作结果
     */
    @Operation(summary = "确认告警", description = "对指定的告警进行确认操作，表示已经知晓该告警")
    @PostMapping("/{alarmId}/ack")
    public WebSocketResponse<String> ackAlarm(
            @Parameter(description = "告警ID", required = true) @PathVariable Long alarmId) {
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
    @Operation(summary = "忽略告警", description = "对指定的告警进行忽略操作，表示不需要处理该告警")
    @PostMapping("/{alarmId}/ignore")
    public WebSocketResponse<String> ignoreAlarm(
            @Parameter(description = "告警ID", required = true) @PathVariable Long alarmId) {
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