package com.open.spring.mvc.geminiFRQgrading;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GeminiWebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // ✅ Allow all your development + production origins
                        .allowedOriginPatterns(
                                "http://localhost:*",
                                "http://127.0.0.1:*",
                                "https://*.github.io",
                                "https://pages.opencodingsociety.com",
                                "https://spring.opencodingsociety.com",
                                "https://flask.opencodingsociety.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}

