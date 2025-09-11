package com.digitaltwin.trial.repository;

import com.digitaltwin.trial.entity.Trial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrialRepository extends JpaRepository<Trial, Long> {
    
    // 根据试验名称模糊查询
    List<Trial> findByNameContainingIgnoreCase(String name);
    
    // 根据试验名称模糊查询（分页）
    Page<Trial> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // 根据试验编号精确查询
    List<Trial> findByRunNo(String runNo);
    
    // 根据试验编号精确查询（分页）
    Page<Trial> findByRunNo(String runNo, Pageable pageable);
    
    // 根据试验名称模糊查询、试验编号精确查询和时间范围过滤（分页）
    @Query("SELECT t FROM Trial t WHERE " +
           "(:name IS NULL OR t.name LIKE %:name%) AND " +
           "(:runNo IS NULL OR t.runNo = :runNo) AND " +
           "(:startTimestamp IS NULL OR t.startTimestamp >= :startTimestamp) AND " +
           "(:endTimestamp IS NULL OR t.startTimestamp <= :endTimestamp)")
    Page<Trial> findByNameContainingIgnoreCaseAndRunNo(
        @Param("name") String name,
        @Param("runNo") String runNo,
        @Param("startTimestamp") Long startTimestamp,
        @Param("endTimestamp") Long endTimestamp,
        Pageable pageable);
    
    // 查询最后一个未结束的试验（按开始时间倒序排列，取第一个）
    @Query("SELECT t FROM Trial t WHERE t.endTimestamp IS NULL ORDER BY t.startTimestamp DESC")
    List<Trial> findLastUnfinishedTrial();
}