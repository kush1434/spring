package com.open.spring.mvc.geminiChatbotTest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GeminiChatTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(columnDefinition = "TEXT")
    private String geminiResponse;

    @Column(nullable = false, updatable = false)
    private Long createdAt;

    public GeminiChatTest(String userId, String userMessage) {
        this.userId = userId;
        this.userMessage = userMessage;
        this.createdAt = System.currentTimeMillis();
    }
}
