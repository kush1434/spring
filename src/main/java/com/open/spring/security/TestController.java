package com.open.spring.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {
    
    @GetMapping("/data")
    public Map<String, Object> getData() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Request successful");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("data", "This endpoint is protected by rate limiting");
        return response;
    }
}