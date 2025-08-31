package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    
    /**
     * 根据点位ID查询其所在分组的所有点位
     * @param pointId 点位ID
     * @return 点位列表
     */
    @Query("SELECT p FROM Point p WHERE p.group.id = (SELECT p2.group.id FROM Point p2 WHERE p2.id = :pointId)")
    List<Point> findPointsInSameGroup(@Param("pointId") Long pointId);
}