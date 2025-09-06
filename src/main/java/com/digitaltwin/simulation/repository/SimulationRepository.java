package com.digitaltwin.simulation.repository;

import com.digitaltwin.simulation.entity.SimulationExperiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationRepository extends JpaRepository<SimulationExperiment, Long> {
    
    /**
     * 根据状态查询试验
     * @param status 试验状态
     * @return 试验列表
     */
    List<SimulationExperiment> findByStatus(String status);
    
    
    /**
     * 根据名称查询试验
     * @param name 试验名称
     * @return 试验
     */
    Optional<SimulationExperiment> findByName(String name);
    
    /**
     * 检查试验名称是否存在
     * @param name 试验名称
     * @return 是否存在
     */
    boolean existsByName(String name);
}