package com.digitaltwin.simulation.repository;

import com.digitaltwin.simulation.entity.UserExperimentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户试验记录Repository接口
 */
@Repository
public interface UserExperimentRecordRepository extends JpaRepository<UserExperimentRecord, Long> {
    
    /**
     * 根据用户ID和试验ID查找记录
     * @param userId 用户ID
     * @param targetExperimentId 试验ID
     * @return 用户试验记录
     */
    Optional<UserExperimentRecord> findByUserIdAndTargetExperimentId(Long userId, Long targetExperimentId);
}