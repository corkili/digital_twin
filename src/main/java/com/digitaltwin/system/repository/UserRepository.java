package com.digitaltwin.system.repository;

import com.digitaltwin.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Optional<User> findByAuapUserId(String auapUserId);
    List<User> findByFullNameContainingIgnoreCase(String fullName);
}