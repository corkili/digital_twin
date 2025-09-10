package com.digitaltwin.device.repository;

import com.digitaltwin.alarm.entity.AlarmState;
import com.digitaltwin.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByName(String name);
    List<Device> findByChannelId(Long channelId);
    boolean existsByName(String name);
    
    @Query("SELECT d FROM Device d WHERE d.id IN :ids")
    List<Device> findByIds(@Param("ids") List<Long> ids);
    
    // 查询有未处理告警的设备（状态为未确认或已确认的告警）
    @Query(value = "SELECT DISTINCT d.* FROM device d " +
            "JOIN point p ON p.device_id = d.id " +
            "JOIN alarm a ON a.point_id = p.id " +
            "WHERE (a.state = 'UNCONFIRMED' OR a.state = 'CONFIRMED') AND d.id = :deviceId", nativeQuery = true)
    Optional<Device> findDeviceWithUnHandledAlarms(@Param("deviceId") Long deviceId);
}