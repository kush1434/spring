package com.open.spring.mvc.geminiChatbot;

import jakarta.persistence.*;
import lombok.*;
import com.open.spring.mvc.geminiFRQgrading.Gemini;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GeminiChat {
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

    public GeminiChat(String userId, String userMessage) {
        this.userId = userId;
        this.userMessage = userMessage;
        this.createdAt = System.currentTimeMillis();
    }
}