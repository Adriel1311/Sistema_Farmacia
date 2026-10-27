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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int requestsPerMinute;
    private final int loginRequestsPerMinute;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RateLimitFilter(
            @Value("${farmamed.rate-limit.requests-per-minute:100}") int requestsPerMinute,
            @Value("${farmamed.rate-limit.login-requests-per-minute:5}") int loginRequestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.loginRequestsPerMinute = loginRequestsPerMinute;

        cleanupExecutor.scheduleAtFixedRate(this::cleanupOldBuckets, 5, 5, TimeUnit.MINUTES);
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

    private void cleanupOldBuckets() {
        long now = Instant.now().toEpochMilli();
        long maxAgeMs = 10 * 60 * 1000; // 10 minutes
        buckets.entrySet().removeIf(entry -> now - entry.getValue().getLastAccess() > maxAgeMs);
    }

    private static class RateLimitBucket {
        private final int maxTokens;
        private final AtomicInteger tokens;
        private final AtomicLong lastRefill;
        private final AtomicLong lastAccess;
        private final long refillIntervalMs = 60_000; // 1 minute

        RateLimitBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = new AtomicInteger(maxTokens);
            long now = Instant.now().toEpochMilli();
            this.lastRefill = new AtomicLong(now);
            this.lastAccess = new AtomicLong(now);
        }

        boolean tryConsume() {
            refillIfNeeded();
            lastAccess.set(Instant.now().toEpochMilli());
            int current = tokens.decrementAndGet();
            return current >= 0;
        }

        private void refillIfNeeded() {
            long now = Instant.now().toEpochMilli();
            long last = lastRefill.get();
            if (now - last >= refillIntervalMs) {
                long elapsed = now - last;
                long intervals = elapsed / refillIntervalMs;
                if (intervals > 0 && lastRefill.compareAndSet(last, last + intervals * refillIntervalMs)) {
                    tokens.updateAndGet(current -> Math.min(maxTokens, current + (int) intervals));
                }
            }
        }

        long getLastAccess() {
            return lastAccess.get();
        }
    }
}