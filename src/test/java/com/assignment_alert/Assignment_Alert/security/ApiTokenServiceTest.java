package com.assignment_alert.Assignment_Alert.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.assignment_alert.Assignment_Alert.user.User;

@ExtendWith(MockitoExtension.class)
class ApiTokenServiceTest {

    private static final long TTL_DAYS = 30L;

    @Mock
    private ApiTokenRepository tokenRepo;

    private ApiTokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new ApiTokenService(tokenRepo);
        ReflectionTestUtils.setField(service, "tokenTtlDays", TTL_DAYS);

        user = new User();
        user.setUserId(4L);
    }

    private static String sha256Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private ApiToken captureSavedToken() {
        ArgumentCaptor<ApiToken> captor = ArgumentCaptor.forClass(ApiToken.class);
        verify(tokenRepo).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void storesOnlyTheHashNeverTheRawToken() throws Exception {
        String raw = service.issueToken(user);
        ApiToken saved = captureSavedToken();

        assertThat(raw).isNotBlank();
        assertThat(saved.getTokenHash())
                .isEqualTo(sha256Hex(raw))
                .hasSize(64)
                .isNotEqualTo(raw);
    }

    @Test
    void revokesExistingTokensBeforeSavingTheNewOne() {
        service.issueToken(user);

        InOrder order = inOrder(tokenRepo);
        order.verify(tokenRepo).deleteByUser(user);
        order.verify(tokenRepo).save(any(ApiToken.class));
    }

    @Test
    void issuesADifferentTokenEveryTime() {
        assertThat(service.issueToken(user)).isNotEqualTo(service.issueToken(user));
    }

    @Test
    void setsCreationAndExpiryFromTheConfiguredLifetime() {
        LocalDateTime before = LocalDateTime.now();
        service.issueToken(user);
        ApiToken saved = captureSavedToken();

        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(saved.getExpiresAt())
                .isAfter(before.plusDays(TTL_DAYS).minusMinutes(1))
                .isBefore(LocalDateTime.now().plusDays(TTL_DAYS).plusMinutes(1));
    }

    @Test
    void resolvesAValidTokenToItsOwner() throws Exception {
        String raw = "a-known-token";
        ApiToken record = new ApiToken();
        record.setUser(user);
        record.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(tokenRepo.findByTokenHash(sha256Hex(raw))).thenReturn(Optional.of(record));

        assertThat(service.resolveUser(raw)).contains(user);
        assertThat(record.getLastUsedAt()).isNotNull();
    }

    @Test
    void treatsAnExpiredTokenAsInvalid() throws Exception {
        String raw = "an-old-token";
        ApiToken record = new ApiToken();
        record.setUser(user);
        record.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(tokenRepo.findByTokenHash(sha256Hex(raw))).thenReturn(Optional.of(record));

        assertThat(service.resolveUser(raw)).isEmpty();
    }

    @Test
    void acceptsATokenThatNeverExpires() throws Exception {
        String raw = "a-permanent-token";
        ApiToken record = new ApiToken();
        record.setUser(user);
        record.setExpiresAt(null);
        when(tokenRepo.findByTokenHash(sha256Hex(raw))).thenReturn(Optional.of(record));

        assertThat(service.resolveUser(raw)).contains(user);
    }

    @Test
    void rejectsAnUnknownToken() {
        when(tokenRepo.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThat(service.resolveUser("forged")).isEmpty();
    }

    @Test
    void rejectsNullAndBlankWithoutTouchingTheDatabase() {
        assertThat(service.resolveUser(null)).isEmpty();
        assertThat(service.resolveUser("")).isEmpty();
        assertThat(service.resolveUser("   ")).isEmpty();

        verify(tokenRepo, never()).findByTokenHash(any());
    }

    @Test
    void looksUpTheSameTokenWithTheSameHashEveryTime() throws Exception {
        String raw = "repeatable";
        when(tokenRepo.findByTokenHash(any())).thenReturn(Optional.empty());

        service.resolveUser(raw);
        service.resolveUser(raw);

        verify(tokenRepo, org.mockito.Mockito.times(2)).findByTokenHash(sha256Hex(raw));
    }

    @Test
    void revokeAllDeletesEveryTokenForTheUser() {
        service.revokeAll(user);

        verify(tokenRepo).deleteByUser(user);
    }
}
