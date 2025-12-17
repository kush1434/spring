package com.example.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    // Read from application.properties
    @Value("${rate.limit.requests.per.user:2}")
    private int requestsPerUser;
    
    @Value("${rate.limit.window.minutes:1}")
    private int windowMinutes;
    
    // Storage: userId -> (windowTimestamp -> count)
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicInteger>> userCounts 
        = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String userId = extractUserId(request);
        
        if (userId == null) {
            response.setStatus(401);
            response.getWriter().write("Authentication required");
            return;
        }
        
        long window = getCurrentWindow();
        int count = getCount(userId, window);
        
        if (count >= requestsPerUser) {
            response.setStatus(429);
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerUser));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        
        increment(userId, window);
        
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerUser));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(requestsPerUser - count - 1));
        
        filterChain.doFilter(request, response);
    }
    
    private String extractUserId(HttpServletRequest request) {
        // Query param (easiest for testing)
        String userId = request.getParameter("userId");
        if (userId != null) return userId;
        
        // Header
        userId = request.getHeader("X-User-Id");
        if (userId != null) return userId;
        
        // Session
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
}