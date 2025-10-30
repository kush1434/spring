package com.open.spring.mvc.geminiFRQgrading;

import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiWebConfig {

    /*
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
    */
}

