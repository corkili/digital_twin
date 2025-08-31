package com.digitaltwin.alarm.repository;

import com.digitaltwin.alarm.entity.AlarmOperateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmOperateLogRepository extends JpaRepository<AlarmOperateLog, Long> {
    List<AlarmOperateLog> findByAlarmIdOrderByOperateTimeDesc(Long alarmId);
    List<AlarmOperateLog> findByAlarmIdOrderByOperateTimeDesc(Long alarmId, org.springframework.data.domain.Pageable pageable);
}