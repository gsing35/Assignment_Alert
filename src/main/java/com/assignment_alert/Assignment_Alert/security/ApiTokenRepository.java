package com.assignment_alert.Assignment_Alert.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.assignment_alert.Assignment_Alert.user.User;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {

    // Llook up record by hased token
    @EntityGraph(attributePaths = "user")
    Optional<ApiToken> findByTokenHash(String tokenHash);

    // Used when reconnecting an account to delete old tokens
    void deleteByUser(User user);
}
