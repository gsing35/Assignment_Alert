package com.assignment_alert.Assignment_Alert.security;

import java.time.LocalDateTime;

import com.assignment_alert.Assignment_Alert.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entity to hold and store hashed tokens
// The purpose of the tokens is so someone can't impersonate and access someone else's rows
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "api_tokens",
        uniqueConstraints = {
            @UniqueConstraint(name = "unique_token_hash", columnNames = "token_hash")
        },
        indexes = {
            @Index(name = "api_token_hash", columnList = "token_hash")
        }
)
public class ApiToken {

    @Id
    @SequenceGenerator(name = "api_token_id_seq", sequenceName = "api_token_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_token_id_seq")
    @Column(nullable = false, updatable = false)
    private Long apiTokenId;

    @Column(name = "token_hash", nullable = false, updatable = false, length = 64)
    private String tokenHash;       

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Refreshed on use
    // TODO clean up ideal tokens
    @Column
    private LocalDateTime lastUsedAt;

    // Null makes it never expire, can still delete it
    @Column
    private LocalDateTime expiresAt;
}
