package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.FaultCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultCategoryRepository extends JpaRepository<FaultCategory, Long> {

    /**
     * 查询所有故障分类，按训练难度排序
     */
    List<FaultCategory> findAllByOrderByTrainingDifficultyAsc();

    /**
     * 根据故障类型查询故障分类
     */
    List<FaultCategory> findByFaultTypeContaining(String faultType);

    /**
     * 根据训练难度查询故障分类
     */
    List<FaultCategory> findByTrainingDifficulty(Integer trainingDifficulty);

    /**
     * 查询指定训练难度范围内的故障分类
     */
    @Query("SELECT fc FROM FaultCategory fc WHERE fc.trainingDifficulty BETWEEN :minDifficulty AND :maxDifficulty ORDER BY fc.trainingDifficulty ASC")
    List<FaultCategory> findByTrainingDifficultyBetween(Integer minDifficulty, Integer maxDifficulty);
}