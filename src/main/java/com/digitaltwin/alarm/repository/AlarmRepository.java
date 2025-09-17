package com.digitaltwin.alarm.repository;

import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByDeviceIdOrderByTimestampDesc(Long deviceId);
    List<Alarm> findByPointIdOrderByTimestampDesc(String pointId);
    List<Alarm> findBySensorIdOrderByTimestampDesc(String sensorId);
    List<Alarm> findByStateOrderByTimestampDesc(AlarmState state);
    List<Alarm> findByStateAndDeviceIdOrderByTimestampDesc(AlarmState state, Long deviceId);

    @Query("SELECT a FROM Alarm a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime ORDER BY a.createdAt DESC")
    List<Alarm> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Alarm a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime ORDER BY a.createdAt DESC")
    List<Alarm> findByTimeRangeWithPagination(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime")
    Long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    Long countByDeviceId(Long deviceId);
    
    @Query("SELECT a FROM Alarm a WHERE a.deviceId = :deviceId ORDER BY a.createdAt DESC")
    List<Alarm> findByDeviceIdWithPagination(@Param("deviceId") Long deviceId, Pageable pageable);
    
    @Query("SELECT a FROM Alarm a ORDER BY a.createdAt DESC")
    List<Alarm> findAllWithPagination(Pageable pageable);
    
    @Query("SELECT a FROM Alarm a WHERE a.deviceId = :deviceId ORDER BY a.timestamp DESC")
    List<Alarm> findLatestByDeviceId(@Param("deviceId") Long deviceId, Pageable pageable);
    
    @Query("SELECT a FROM Alarm a WHERE a.pointId = :pointId AND a.alarmType = :alarmType AND (a.endTimestamp IS NULL OR a.endTimestamp = 0) ORDER BY a.timestamp DESC")
    List<Alarm> findUnendedAlarmsByPointIdAndAlarmType(@Param("pointId") String pointId, @Param("alarmType") String alarmType);
    
    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.pointId = :pointId AND a.timestamp >= :startTime")
    Long countRecentAlarmsByPointId(@Param("pointId") String pointId, @Param("startTime") Long startTime);
}