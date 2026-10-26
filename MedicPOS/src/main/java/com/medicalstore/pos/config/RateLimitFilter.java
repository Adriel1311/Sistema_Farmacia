package com.medicalstore.pos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int requestsPerMinute;
    private final int loginRequestsPerMinute;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${farmamed.rate-limit.requests-per-minute:100}") int requestsPerMinute,
            @Value("${farmamed.rate-limit.login-requests-per-minute:5}") int loginRequestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.loginRequestsPerMinute = loginRequestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        String key = clientIp + ":" + path;
        int limit = getLimitForPath(path);

        RateLimitBucket bucket = buckets.computeIfAbsent(key, k -> new RateLimitBucket(limit));

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private int getLimitForPath(String path) {
        if (path.contains("/api/auth/login")) {
            return loginRequestsPerMinute;
        }
        if (path.contains("/api/dni/")) {
            return 30;
        }
        return requestsPerMinute;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitBucket {
        private final int maxTokens;
        private final AtomicInteger tokens;
        private volatile long lastRefill = Instant.now().toEpochMilli();
        private final long refillIntervalMs = 60_000; // 1 minute

        RateLimitBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = new AtomicInteger(maxTokens);
        }

        boolean tryConsume() {
            refillIfNeeded();
            int current = tokens.get();
            if (current > 0) {
                return tokens.decrementAndGet() >= 0;
            }
            return false;
        }

        private void refillIfNeeded() {
            long now = Instant.now().toEpochMilli();
            if (now - lastRefill >= refillIntervalMs) {
                long elapsed = now - lastRefill;
                long intervals = elapsed / refillIntervalMs;
                if (intervals > 0) {
                    int newTokens = Math.min(maxTokens, tokens.get() + (int) intervals);
                    tokens.set(newTokens);
                    lastRefill += intervals * refillIntervalMs;
                }
            }
        }
    }
}