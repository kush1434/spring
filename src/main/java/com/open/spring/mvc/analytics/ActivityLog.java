package com.open.spring.mvc.analytics;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.open.spring.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ActivityLog entity tracks individual student activity events across multiple sources.
 * Each event is logged with a timestamp, source, event type, and optional metadata.
 * 
 * Data format follows the specification:
 * {
 *   "github_login": "jm1021",
 *   "source": "opencs | github | calendar",
 *   "course": "CSA",
 *   "artifact": "arrays-lesson",
 *   "event_type": "page_view | commit | issue | lesson_complete",
 *   "event_weight": 1.0,
 *   "timestamp": "2026-01-12T18:42:00Z"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the Person (student) who performed this activity.
     * Uses github_login (uid field in Person) as the canonical identifier.
     */
    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    @NotNull
    private Person person;

    /**
     * Source system where the activity occurred.
     * Valid values: "opencs", "github", "calendar"
     */
    @NotEmpty
    @Column(nullable = false)
    private String source;

    /**
     * Course identifier (e.g., "CSA", "CSP", "CSSE")
     */
    @NotEmpty
    @Column(nullable = false)
    private String course;

    /**
     * Artifact or resource that was interacted with.
     * Examples: "arrays-lesson", "repo-name", "event-name"
     */
    private String artifact;

    /**
     * Type of event that occurred.
     * Valid values: "page_view", "commit", "issue", "pull_request", "lesson_complete", "lesson_start"
     */
    @NotEmpty
    @Column(nullable = false)
    private String eventType;

    /**
     * Weight/importance of this event for metrics calculation.
     * Default: 1.0
     */
    @Column(nullable = false)
    private Double eventWeight = 1.0;

    /**
     * Timestamp when the activity occurred.
     * Automatically set to current time if not provided.
     */
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    /**
     * Additional metadata as JSON.
     * Can store source-specific details like commit hash, page URL, etc.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Set timestamp to current time if not already set.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Constructor for creating activity logs with minimal required fields.
     */
    public ActivityLog(Person person, String source, String course, String artifact, String eventType) {
        this.person = person;
        this.source = source;
        this.course = course;
        this.artifact = artifact;
        this.eventType = eventType;
        this.eventWeight = 1.0;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with event weight.
     */
    public ActivityLog(Person person, String source, String course, String artifact, 
                       String eventType, Double eventWeight) {
        this(person, source, course, artifact, eventType);
        this.eventWeight = eventWeight;
    }
}
