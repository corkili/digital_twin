package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.DeviceOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceOperationLogRepository extends JpaRepository<DeviceOperationLog, Long> {
    List<DeviceOperationLog> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
    
    List<DeviceOperationLog> findByOperatorIdOrderByCreatedAtDesc(Long operatorId);
    
    List<DeviceOperationLog> findAllByOrderByCreatedAtDesc();
}