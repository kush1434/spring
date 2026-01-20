package com.open.spring.mvc.analytics;

import java.time.LocalDate;
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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WeeklyAnalytics entity stores computed weekly metrics for each student.
 * Generated each Sunday at 12:01 AM for students with activity during the prior week.
 * 
 * Metrics are indicators for Live Review context, not grades.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WeeklyAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the student (Person).
     */
    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    @NotNull
    private Person person;

    /**
     * Course identifier (e.g., "CSA", "CSP").
     */
    @NotNull
    @Column(nullable = false)
    private String course;

    /**
     * Week start date (Monday of the week being analyzed).
     */
    @NotNull
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    /**
     * Week end date (Sunday of the week being analyzed).
     */
    @NotNull
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnd;

    /**
     * Timestamp when this analytics record was generated.
     */
    @Column(nullable = false)
    private LocalDateTime generatedAt;

    // ===== GitHub Contribution Indicators =====

    /**
     * Number of commits during the week.
     */
    @Column(nullable = false)
    private Integer commits = 0;

    /**
     * Number of pull requests during the week.
     */
    @Column(nullable = false)
    private Integer pullRequests = 0;

    /**
     * Number of issues created/interacted during the week.
     */
    @Column(nullable = false)
    private Integer issues = 0;

    // ===== OpenCS Engagement Indicators =====

    /**
     * Total page views on OpenCS during the week.
     */
    @Column(nullable = false)
    private Integer totalPageViews = 0;

    /**
     * Unique page views (distinct pages) on OpenCS.
     */
    @Column(nullable = false)
    private Integer uniquePageViews = 0;

    /**
     * Number of lessons started.
     */
    @Column(nullable = false)
    private Integer lessonsStarted = 0;

    /**
     * Number of lessons completed.
     */
    @Column(nullable = false)
    private Integer lessonsCompleted = 0;

    // ===== Calendar Context =====

    /**
     * Number of instructional school days during the week.
     */
    @Column(nullable = false)
    private Integer schoolDays = 0;

    // ===== Derived Metrics =====

    /**
     * Engagement score: (views + completions) / school_days
     * Formula: engagement_score = (totalPageViews + lessonsCompleted) / schoolDays
     */
    @Column(nullable = false)
    private Double engagementScore = 0.0;

    /**
     * Contribution score: commits*3 + prs*4 + issues*3
     * Formula: contribution_score = commits*3 + pullRequests*4 + issues*3
     */
    @Column(nullable = false)
    private Double contributionScore = 0.0;

    /**
     * Combined week score: engagement_score + contribution_score
     * Formula: week_score = engagementScore + contributionScore
     */
    @Column(nullable = false)
    private Double weekScore = 0.0;

    /**
     * Consistency score based on 4-week rolling window.
     * Formula: 0.6 * stability_score + 0.4 * ((trend_score + 1) / 2)
     * Calculated by WeeklyAnalyticsService.
     */
    @Column
    private Double consistencyScore;

    /**
     * Trend direction: "increasing", "stable", "declining"
     * Based on week-over-week comparison.
     */
    @Column
    private String trendDirection;

    /**
     * Percentile rank within course (0-100).
     * Calculated after all weekly analytics are generated.
     */
    @Column
    private Double percentileRank;

    /**
     * Additional metadata as JSON for future extensibility.
     * Can store breakdown by source, artifact details, etc.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Whether this student was considered "active" for this week.
     * Active means events.count > threshold (e.g., 5 events).
     */
    @Column(nullable = false)
    private Boolean isActive = false;

    /**
     * Calculate derived metrics based on raw counts.
     */
    public void calculateDerivedMetrics() {
        // Engagement score: (views + completions) / school_days
        if (schoolDays > 0) {
            this.engagementScore = (double) (totalPageViews + lessonsCompleted) / schoolDays;
        } else {
            this.engagementScore = 0.0;
        }

        // Contribution score: commits*3 + prs*4 + issues*3
        this.contributionScore = (commits * 3.0) + (pullRequests * 4.0) + (issues * 3.0);

        // Week score: engagement + contribution
        this.weekScore = engagementScore + contributionScore;
    }
}
