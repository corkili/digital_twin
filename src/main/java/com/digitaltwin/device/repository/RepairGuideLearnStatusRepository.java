package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.RepairGuideLearnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairGuideLearnStatusRepository extends JpaRepository<RepairGuideLearnStatus, Long> {
    Optional<RepairGuideLearnStatus> findByGuideIdAndUserId(Long guideId, Long userId);
    List<RepairGuideLearnStatus> findByUserId(Long userId);
}