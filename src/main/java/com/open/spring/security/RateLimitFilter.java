package com.open.spring.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // User is considered "active" if they made a request in last 5 minutes
    private static final long ACTIVE_USER_TIMEOUT_MS = 5 * 60 * 1000;

    // Track per-user buckets and limits
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Integer> userLimits = new ConcurrentHashMap<>();

    // Track active users with last-seen timestamp
    private final Map<String, Long> activeUsers = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userKey = getUserKey(request);

        // Update last-seen timestamp
        activeUsers.put(userKey, System.currentTimeMillis());

        // Remove inactive users
        cleanupInactiveUsers();

        int activeUserCount = activeUsers.size();
        int dynamicLimit = computeDynamicLimit(activeUserCount);

        // Rebuild bucket if limit changed
        Integer previousLimit = userLimits.get(userKey);
        if (previousLimit == null || previousLimit != dynamicLimit) {
            buckets.put(userKey, createBucket(dynamicLimit));
            userLimits.put(userKey, dynamicLimit);
        }

        Bucket bucket = buckets.get(userKey);

        // Expose rate-limit info (helps debugging + clients)
        response.setHeader("X-Rate-Limit", String.valueOf(dynamicLimit));
        response.setHeader("X-Active-Users", String.valueOf(activeUserCount));

        // Debug log (remove in prod)
        System.out.println(
                "User=" + userKey +
                " | ActiveUsers=" + activeUserCount +
                " | RateLimit=" + dynamicLimit
        );

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write(
                    "Too many requests. Limit: " + dynamicLimit + " requests per minute."
            );
        }
    }

    // ---------- Helpers ----------

    private String getUserKey(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return request.getRemoteAddr(); // fallback for unauthenticated users
    }

    private void cleanupInactiveUsers() {
        long now = System.currentTimeMillis();
        activeUsers.entrySet().removeIf(
                entry -> now - entry.getValue() > ACTIVE_USER_TIMEOUT_MS
        );
    }

    private Bucket createBucket(int limit) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillGreedy(limit, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * More users → lower per-user rate limit
     */
    private int computeDynamicLimit(int activeUsers) {
        if (activeUsers < 5) {
            return 600;   // very low load
        } else if (activeUsers < 20) {
            return 300;   // moderate load
        } else {
            return 100;   // high load
        }
    }
}
