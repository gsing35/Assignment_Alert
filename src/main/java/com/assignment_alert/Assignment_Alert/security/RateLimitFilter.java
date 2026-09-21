package com.assignment_alert.Assignment_Alert.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(int limit, Duration window) {
    }

    private static final class Window {
        private final long startMillis;
        private final AtomicInteger count = new AtomicInteger();

        private Window(long startMillis) {
            this.startMillis = startMillis;
        }
    }

    private static final Rule CONNECT = new Rule(5, Duration.ofHours(1));
    private static final Rule SYNC = new Rule(10, Duration.ofHours(1));
    private static final Rule DEFAULT = new Rule(120, Duration.ofMinutes(1));

    private static final long LONGEST_WINDOW_MILLIS = Duration.ofHours(1).toMillis();
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        Rule rule;
        String key;

        if ("POST".equalsIgnoreCase(method) && path.equals("/api/v1/canvas/connect")) {
            rule = CONNECT;
            key = "connect:ip:" + request.getRemoteAddr();
        } else {
            String caller = callerKey(request);
            if ("PUT".equalsIgnoreCase(method) && path.startsWith("/api/v1/courses/")) {
                rule = SYNC;
                key = "sync:" + caller;
            } else {
                rule = DEFAULT;
                key = "api:" + caller;
            }
        }

        long retryAfterSeconds = tryAcquire(key, rule);
        if (retryAfterSeconds > 0) {
            long retryAfterMinutes = (retryAfterSeconds + 59) / 60;
            String unit = retryAfterMinutes == 1 ? " minute." : " minutes.";

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"message\":\"Too many requests. Try again in " + retryAfterMinutes + unit + "\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String callerKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return "user:" + user.userId();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private long tryAcquire(String key, Rule rule) {
        long now = System.currentTimeMillis();
        long windowMillis = rule.window().toMillis();

        if (windows.size() > MAX_TRACKED_KEYS) {
            windows.values().removeIf(w -> now - w.startMillis >= LONGEST_WINDOW_MILLIS);
        }

        Window window = windows.compute(key, (k, existing) ->
                existing == null || now - existing.startMillis >= windowMillis ? new Window(now) : existing);

        if (window.count.incrementAndGet() <= rule.limit()) {
            return 0;
        }

        long remainingMillis = windowMillis - (now - window.startMillis);
        return Math.max(1, (remainingMillis + 999) / 1000);
    }
}
