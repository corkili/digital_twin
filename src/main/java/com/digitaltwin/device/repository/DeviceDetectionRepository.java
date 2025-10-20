package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.DeviceDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceDetectionRepository extends JpaRepository<DeviceDetection, Long> {

    /**
     * 根据设备ID查询检测数据列表
     */
    List<DeviceDetection> findByDeviceIdOrderByParameterNameAsc(Long deviceId);

    /**
     * 查询所有检测数据列表，按设备ID和参数名称排序
     */
    List<DeviceDetection> findAllByOrderByDeviceIdAscParameterNameAsc();
}