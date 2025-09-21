package com.digitaltwin.simulation.repository;

import com.digitaltwin.simulation.entity.EmergencyProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyProcedureRepository extends JpaRepository<EmergencyProcedure, Long> {

    /**
     * 根据状态查询应急流程
     * @param status 状态
     * @return 应急流程列表
     */
    List<EmergencyProcedure> findByStatus(String status);
}