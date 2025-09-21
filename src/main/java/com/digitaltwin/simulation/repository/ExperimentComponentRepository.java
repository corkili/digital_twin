package com.digitaltwin.simulation.repository;

import com.digitaltwin.simulation.entity.ExperimentComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentComponentRepository extends JpaRepository<ExperimentComponent, Long> {

    /**
     * 根据状态查询试验组件
     * @param status 状态
     * @return 试验组件列表
     */
    List<ExperimentComponent> findByStatus(String status);
}