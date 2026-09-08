package com.assignment_alert.Assignment_Alert.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserId(Long userId);
    Optional<User> findByCanvasId(Long canvasId);
    Boolean existsByCanvasId(Long canvasId);
    //grgrergrg
}

