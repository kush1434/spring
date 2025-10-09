package com.open.spring.mvc.lessonProgress;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lesson_progress")
@Data
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String lessonKey;

    // Time tracking
    @Column(nullable = false)
    private Long totalTimeMs = 0L;

    @Column
    private LocalDateTime lastVisited;

    // Progress tracking
    @Column(nullable = false)
    private Boolean completed = false;

    // Badges
    @ElementCollection
    @CollectionTable(name = "lesson_badges", joinColumns = @JoinColumn(name = "lesson_progress_id"))
    @Column(name = "badge_name")
    private Set<String> badges = new HashSet<>();

    // Current flashcard index (optional)
    @Column
    private Integer currentFlashcardIndex = 1;

    // Reflection / Notes
    @Column(columnDefinition = "TEXT")
    private String reflectionText;

    // Metadata
    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
