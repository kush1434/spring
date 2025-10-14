package com.open.spring.mvc.quiz;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "quiz_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizScore {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
