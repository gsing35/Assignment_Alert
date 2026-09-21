package com.assignment_alert.Assignment_Alert.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignment_alert.Assignment_Alert.user.User;

import lombok.RequiredArgsConstructor;

// Servce for ApiToken.java, It issues and verifies the tokens.
@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // 256 bits

    private final ApiTokenRepository tokenRepo;

    // Create a new token for a user, and delete any old ones, only time the raw token is passed
    @Transactional
    public String issueToken(User user) {
        tokenRepo.deleteByUser(user);       // Delete old token to ensure each user gets only one token to ensure best security

        byte[] raw = new byte[TOKEN_BYTES];         // random bytes which serve as the token to tell if user is actually user
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);     // Encode it to be URL safe: getUrlEncoder() will make it safe url by removing characters like +, /, etc. withoutPadding() removes padding at the end to make it cleaner, and encodeToString() turns the bytes into a string, the rest clean it up.

        ApiToken record = new ApiToken();       // Create an ApiToken Obj with the hashed String token, user who it belongs to, and time made
        record.setTokenHash(hash(token));
        record.setUser(user);
        record.setCreatedAt(LocalDateTime.now());

        tokenRepo.save(record);
        return token;           // Return to actual token to the frontend, only time it will be used
    }

    // Resolves the raw token back to the user, chcks if the token expirery date is till valid, and updates last used time
    @Transactional
    public Optional<User> resolveUser(String token) {
        if (token == null || token.isBlank()) {     // Check if the token is valid if so return empty.
            return Optional.empty();
        }

        return tokenRepo.findByTokenHash(hash(token))   // Find the record by the hashed token
                .filter(record -> record.getExpiresAt() == null || record.getExpiresAt().isAfter(LocalDateTime.now()))      // Check if the expirey date on the record is still valid if not return empty
                .map(record -> {    
                    record.setLastUsedAt(LocalDateTime.now());          // Update last used and return user of the record
                    return record.getUser();
                });
    }

    // Deletes the users token
    @Transactional
    public void revokeAll(User user) {
        tokenRepo.deleteByUser(user);       
    }

    // Helper method to hash the token
    private String hash(String token) {
        try {       // MessageDigest requires try catch block
            MessageDigest digest = MessageDigest.getInstance("SHA-256");        // Hashing algorithim
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));      // Convert the token String to bytes to hash it

            StringBuilder hex = new StringBuilder(hashed.length * 2);       // Each bytes needs 2 hex digits 0xff
            for (byte b : hashed) { 
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));         // Big-endian hex: isoltae top 4 bits of the hex by shifting and &ing
                hex.append(Character.forDigit(b & 0xF, 16));                // isolate low 4 bits
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);           
        }
    }
}
