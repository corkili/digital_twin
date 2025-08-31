package com.digitaltwin.device.repository;

import com.digitaltwin.device.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByIdentity(String identity);
    
    @Query("SELECT p FROM Point p WHERE p.identity = :identity AND p.device.id = :deviceId")
    Optional<Point> findByIdentityAndDeviceId(@Param("identity") String identity, @Param("deviceId") Long deviceId);
    
    @Query("SELECT p FROM Point p WHERE p.identity IN :identities")
    List<Point> findByIdentityIn(@Param("identities") List<String> identities);
}