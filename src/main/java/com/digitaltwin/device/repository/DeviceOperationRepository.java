package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.DeviceOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceOperationRepository extends JpaRepository<DeviceOperation, Long> {

    /**
     * 根据设备ID分页查询操作记录
     */
    Page<DeviceOperation> findByDeviceIdOrderByOperationTimeDesc(Long deviceId, Pageable pageable);

    /**
     * 根据设备ID查询操作记录（不分页）
     */
    List<DeviceOperation> findByDeviceIdOrderByOperationTimeDesc(Long deviceId);

}