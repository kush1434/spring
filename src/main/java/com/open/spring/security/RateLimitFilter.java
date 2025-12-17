package com.open.spring.security;

<<<<<<< HEAD
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
=======
>>>>>>> 6ab95f88 (ratelimit)
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
<<<<<<< HEAD

    @Value("${security.rate-limit.requests-per-minute:20}")
    private int requestsPerMinute;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> userLimits = new ConcurrentHashMap<>();
    private final Map<String, Boolean> activeUsers = new ConcurrentHashMap<>();

=======
    
    @Value("${rate.limit.requests.per.user:2}")
    private int requestsPerUser;
    
    @Value("${rate.limit.window.minutes:1}")
    private int windowMinutes;
    
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicInteger>> userCounts 
        = new ConcurrentHashMap<>();
    
>>>>>>> 6ab95f88 (ratelimit)
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
<<<<<<< HEAD

        String username = request.getUserPrincipal() != null
                ? request.getUserPrincipal().getName()
                : request.getRemoteAddr();

        // Track logged-in users
        if (request.getUserPrincipal() != null) {
            activeUsers.put(username, true);
        }

        int activeUserCount = activeUsers.size();
        int dynamicLimit = computeDynamicLimit(username, activeUserCount);

        // Check if limit has changed for this user
        Integer previousLimit = userLimits.get(username);
        if (previousLimit == null || previousLimit != dynamicLimit) {
            // Recreate bucket with new limit
            cache.put(username, createBucketWithLimit(dynamicLimit));
            userLimits.put(username, dynamicLimit);
        }

        Bucket bucket = cache.get(username);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write("Too many requests - limit: " + dynamicLimit + " per minute");
=======
        
        String userId = extractUserId(request);
        
        if (userId == null) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication required\"}");
            return;
        }
        
        long window = getCurrentWindow();
        int count = getCount(userId, window);
        
        if (count >= requestsPerUser) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerUser));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            return;
>>>>>>> 6ab95f88 (ratelimit)
        }
        
        increment(userId, window);
        
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerUser));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(requestsPerUser - count - 1));
        
        filterChain.doFilter(request, response);
    }
<<<<<<< HEAD

    private Bucket createBucketWithLimit(int limit) {
        // Use Bandwidth.builder() instead of Bandwidth.classic()
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillGreedy(limit, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    private int computeDynamicLimit(String username, int activeUsers) {
        if (activeUsers < 5) {
            return 100;
        } else if (activeUsers < 20) {
            return 300;
        } else {
            return 600;
        }
    }
=======
    
    private String extractUserId(HttpServletRequest request) {
        String userId = request.getParameter("userId");
        if (userId != null) return userId;
        
        userId = request.getHeader("X-User-Id");
        if (userId != null) return userId;
        
        if (request.getSession(false) != null) {
            Object userIdObj = request.getSession().getAttribute("userId");
            if (userIdObj != null) return userIdObj.toString();
        }
        
        return null;
    }
    
    private int getCount(String userId, long window) {
        return userCounts
            .getOrDefault(userId, new ConcurrentHashMap<>())
            .getOrDefault(window, new AtomicInteger(0))
            .get();
    }
    
    private void increment(String userId, long window) {
        userCounts
            .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(window, k -> new AtomicInteger(0))
            .incrementAndGet();
    }
    
    private long getCurrentWindow() {
        return System.currentTimeMillis() / (windowMinutes * 60 * 1000L);
    }
>>>>>>> 6ab95f88 (ratelimit)
}