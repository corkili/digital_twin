package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByIdentity(String identity);
    
    @Query("SELECT p FROM Point p WHERE p.group.id = :groupId " +
           "AND (:pointName IS NULL OR p.identity LIKE %:pointName%) " +
           "AND (:deviceName IS NULL OR p.device.name LIKE %:deviceName%)")
    Page<Point> findPointsByGroupAndFilters(
        @Param("groupId") Long groupId,
        @Param("pointName") String pointName,
        @Param("deviceName") String deviceName,
        Pageable pageable
    );
}