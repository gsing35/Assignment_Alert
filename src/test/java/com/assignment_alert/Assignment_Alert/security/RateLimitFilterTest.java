package com.assignment_alert.Assignment_Alert.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class RateLimitFilterTest {

    private static final String CONNECT = "/api/v1/canvas/connect";
    private static final String SYNC = "/api/v1/courses/177530";
    private static final String ASSIGNMENTS = "/api/v1/assignments";

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(userId),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private MockHttpServletResponse call(String method, String path, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private int status(String method, String path, String ip) throws Exception {
        return call(method, path, ip).getStatus();
    }

    @Test
    void allowsFiveConnectsThenRefusesTheSixth() throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThat(status("POST", CONNECT, "10.1.1.1")).as("attempt %d", attempt).isEqualTo(200);
        }

        MockHttpServletResponse refused = call("POST", CONNECT, "10.1.1.1");

        assertThat(refused.getStatus()).isEqualTo(429);
        assertThat(refused.getHeader("Retry-After")).isNotNull();
        assertThat(refused.getContentAsString())
                .contains("Too many requests")
                .contains("minute")
                .doesNotContain("seconds");
    }

    @Test
    void connectLimitIsCountedPerIpAddress() throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            status("POST", CONNECT, "10.1.1.1");
        }

        assertThat(status("POST", CONNECT, "10.1.1.1")).isEqualTo(429);
        assertThat(status("POST", CONNECT, "10.2.2.2")).isEqualTo(200);
    }

    @Test
    void allowsTenCourseSyncsThenRefusesTheEleventh() throws Exception {
        authenticateAs(4L);

        for (int attempt = 1; attempt <= 10; attempt++) {
            assertThat(status("PUT", SYNC, "10.1.1.1")).as("attempt %d", attempt).isEqualTo(200);
        }

        assertThat(status("PUT", SYNC, "10.1.1.1")).isEqualTo(429);
    }

    @Test
    void syncLimitIsCountedPerUserNotPerIp() throws Exception {
        authenticateAs(4L);
        for (int attempt = 1; attempt <= 10; attempt++) {
            status("PUT", SYNC, "10.1.1.1");
        }
        assertThat(status("PUT", SYNC, "10.1.1.1")).isEqualTo(429);

        authenticateAs(5L);
        assertThat(status("PUT", SYNC, "10.1.1.1")).isEqualTo(200);
    }

    @Test
    void ordinaryRequestsUseTheGenerousLimit() throws Exception {
        authenticateAs(4L);

        for (int attempt = 1; attempt <= 60; attempt++) {
            assertThat(status("GET", ASSIGNMENTS, "10.1.1.1")).as("attempt %d", attempt).isEqualTo(200);
        }
    }

    @Test
    void preflightRequestsAreNeverLimited() throws Exception {
        for (int attempt = 1; attempt <= 20; attempt++) {
            assertThat(status("OPTIONS", CONNECT, "10.1.1.1")).as("attempt %d", attempt).isEqualTo(200);
        }
    }

    @Test
    void theCounterResetsOnceTheWindowHasPassed() throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            status("POST", CONNECT, "10.1.1.1");
        }
        assertThat(status("POST", CONNECT, "10.1.1.1")).isEqualTo(429);

        backdateAllWindows();

        assertThat(status("POST", CONNECT, "10.1.1.1")).isEqualTo(200);
    }

    @SuppressWarnings("unchecked")
    private void backdateAllWindows() {
        Map<String, Object> windows =
                (Map<String, Object>) ReflectionTestUtils.getField(filter, "windows");

        long twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000L);
        for (Object window : windows.values()) {
            ReflectionTestUtils.setField(window, "startMillis", twoHoursAgo);
        }
    }
}
