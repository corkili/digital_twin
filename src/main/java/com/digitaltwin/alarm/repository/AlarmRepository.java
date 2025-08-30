package com.digitaltwin.alarm.repository;

import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByDeviceIdOrderByTimestampDesc(Long deviceId);
    List<Alarm> findByPointIdOrderByTimestampDesc(String pointId);
    List<Alarm> findBySensorIdOrderByTimestampDesc(String sensorId);
    List<Alarm> findByStateOrderByTimestampDesc(AlarmState state);
    List<Alarm> findByStateAndDeviceIdOrderByTimestampDesc(AlarmState state, Long deviceId);
}