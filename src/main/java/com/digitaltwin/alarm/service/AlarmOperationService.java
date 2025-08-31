package com.digitaltwin.alarm.service;

import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmOperateLog;
import com.digitaltwin.alarm.entity.AlarmState;
import com.digitaltwin.alarm.repository.AlarmOperateLogRepository;
import com.digitaltwin.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmOperationService {
    
    private final AlarmRepository alarmRepository;
    private final AlarmOperateLogRepository alarmOperateLogRepository;
    
    /**
     * 确认告警
     * 
     * @param alarmId 告警ID
     * @return 操作是否成功
     */
    public boolean acknowledgeAlarm(Long alarmId) {
        try {
            Optional<Alarm> alarmOptional = alarmRepository.findById(alarmId);
            if (alarmOptional.isPresent()) {
                Alarm alarm = alarmOptional.get();
                alarm.setState(AlarmState.CONFIRMED);
                alarmRepository.save(alarm);
                
                // 保存操作日志
                AlarmOperateLog operateLog = new AlarmOperateLog();
                operateLog.setAlarmId(alarmId);
                operateLog.setOperateAction("确认");
                alarmOperateLogRepository.save(operateLog);
                
                log.info("告警 {} 已确认", alarmId);
                return true;
            } else {
                log.warn("未找到ID为 {} 的告警", alarmId);
                return false;
            }
        } catch (Exception e) {
            log.error("确认告警时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 忽略告警
     * 
     * @param alarmId 告警ID
     * @return 操作是否成功
     */
    public boolean ignoreAlarm(Long alarmId) {
        try {
            Optional<Alarm> alarmOptional = alarmRepository.findById(alarmId);
            if (alarmOptional.isPresent()) {
                Alarm alarm = alarmOptional.get();
                alarm.setState(AlarmState.IGNORED);
                alarmRepository.save(alarm);
                
                // 保存操作日志
                AlarmOperateLog operateLog = new AlarmOperateLog();
                operateLog.setAlarmId(alarmId);
                operateLog.setOperateAction("忽略");
                alarmOperateLogRepository.save(operateLog);
                
                log.info("告警 {} 已忽略", alarmId);
                return true;
            } else {
                log.warn("未找到ID为 {} 的告警", alarmId);
                return false;
            }
        } catch (Exception e) {
            log.error("忽略告警时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
}