
package com.open.spring.mvc.lessonProgress;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "lesson_progress")
@Data
public class LessonProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId; // or Long userId with @ManyToOne User relationship
    
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
    
    // Flashcards
    @OneToMany(mappedBy = "lessonProgress", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardProgress> flashcardProgress = new ArrayList<>();
    
    @Column
    private Integer currentFlashcardIndex = 1;
    
    // Reflection/Notes
    @Column(columnDefinition = "TEXT")
    private String reflectionText;
    
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

@Entity
@Table(name = "flashcard_progress")
@Data
class FlashcardProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_progress_id", nullable = false)
    private LessonProgress lessonProgress;
    
    @Column(nullable = false)
    private Integer cardIndex;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status;
    
    @Column
    private LocalDateTime markedAt;
    
    public enum CardStatus {
        KNOWN,
        REVIEW,
        UNSEEN
    }
}