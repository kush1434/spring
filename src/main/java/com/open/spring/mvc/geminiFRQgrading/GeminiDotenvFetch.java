package com.open.spring.mvc.geminiFRQgrading;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiDotenvFetch {
    private final Dotenv dotenv = Dotenv.load();
    @Bean
    public String geminiApiUrl() {
        return dotenv.get("GEMINI_API_URL");
    }
    @Bean
    public String geminiApiKey() {
        return dotenv.get("GEMINI_API_KEY");
    }
}
