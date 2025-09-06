package com.digitaltwin.simulation.repository;

import com.digitaltwin.simulation.entity.ExamRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<ExamRecord, Long> {
    List<ExamRecord> findByNameContainingIgnoreCase(String name);
    Page<ExamRecord> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
