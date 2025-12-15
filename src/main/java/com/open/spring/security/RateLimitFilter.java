package com.open.spring.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${security.rate-limit.requests-per-minute:20}")
    private int requestsPerMinute;

    @Value("${security.rate-limit.user-timeout-minutes:30}")
    private int userTimeoutMinutes;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> userLimits = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastActivityTime = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = getUserKey(request);
        
        // Update last activity time
        lastActivityTime.put(key, Instant.now());

        int activeUserCount = lastActivityTime.size();
        int dynamicLimit = computeDynamicLimit(key, activeUserCount);

        // Get or create bucket
        Bucket bucket = cache.computeIfAbsent(key, k -> {
            userLimits.put(k, dynamicLimit);
            return createBucketWithLimit(dynamicLimit);
        });

        // Check if limit needs updating (without recreating bucket)
        Integer previousLimit = userLimits.get(key);
        if (previousLimit != null && previousLimit != dynamicLimit) {
            // Note: Bucket4j doesn't support dynamic limit changes well
            // For production, consider using a distributed cache like Redis
            // For now, we keep the existing bucket to maintain state
            userLimits.put(key, dynamicLimit);
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Too many requests\",\"limit\":%d,\"window\":\"1 minute\"}",
                dynamicLimit
            ));
        }
    }

    /**
     * Get unique key for rate limiting.
     * Prioritizes username, falls back to IP address.
     */
    private String getUserKey(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return "user:" + request.getUserPrincipal().getName();
        }
        
        // Check for proxy headers
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            // Take first IP if multiple proxies
            ip = ip.split(",")[0].trim();
        }
        
        return "ip:" + ip;
    }

    private Bucket createBucketWithLimit(int limit) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillGreedy(limit, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Compute dynamic limit based on active users.
     * Can be customized based on your needs.
     */
    private int computeDynamicLimit(String key, int activeUsers) {
        // Give authenticated users higher limits
        boolean isAuthenticated = key.startsWith("user:");
        
        int baseLimit;
        if (activeUsers < 5) {
            baseLimit = 100;
        } else if (activeUsers < 20) {
            baseLimit = 300;
        } else {
            baseLimit = 600;
        }
        
        // Reduce limit for unauthenticated users
        return isAuthenticated ? baseLimit : baseLimit / 2;
    }

    /**
     * Clean up inactive users every 5 minutes.
     * Prevents memory leaks from accumulating user data.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupInactiveUsers() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(userTimeoutMinutes));
        
        lastActivityTime.entrySet().removeIf(entry -> {
            boolean isInactive = entry.getValue().isBefore(cutoff);
            if (isInactive) {
                String key = entry.getKey();
                cache.remove(key);
                userLimits.remove(key);
            }
            return isInactive;
        });
    }
}