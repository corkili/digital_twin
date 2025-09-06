package com.digitaltwin.simulation.repository;

import com.digitaltwin.simulation.entity.VirtualScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 虚拟场景Repository
 */
@Repository
public interface VirtualSceneRepository extends JpaRepository<VirtualScene, Long> {
    
    /**
     * 根据状态查询场景
     * @param status 场景状态
     * @return 场景列表
     */
    List<VirtualScene> findByStatus(String status);
    
    /**
     * 根据名称查询场景
     * @param name 场景名称
     * @return 场景
     */
    Optional<VirtualScene> findByName(String name);
    
    /**
     * 检查场景名称是否存在
     * @param name 场景名称
     * @return 是否存在
     */
    boolean existsByName(String name);
}