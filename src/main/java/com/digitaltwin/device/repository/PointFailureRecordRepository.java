package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.PointFailureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointFailureRecordRepository extends JpaRepository<PointFailureRecord, Long> {
    
    /**
     * 查询指定日期范围内的故障记录数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 故障记录数量
     */
    @Query("SELECT COUNT(p) FROM PointFailureRecord p WHERE p.failureTime >= :startTime AND p.failureTime < :endTime")
    long countByFailureTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询最近7天的故障记录
     * @param startTime 开始时间
     * @return 故障记录列表
     */
    List<PointFailureRecord> findByFailureTimeAfter(LocalDateTime startTime);
}