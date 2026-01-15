package com.open.spring.mvc.analytics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.open.spring.mvc.person.Person;

/**
 * JPA Repository for WeeklyAnalytics entity.
 */
public interface WeeklyAnalyticsJpaRepository extends JpaRepository<WeeklyAnalytics, Long> {

    /**
     * Find all analytics records for a specific person.
     */
    List<WeeklyAnalytics> findByPerson(Person person);

    /**
     * Find all analytics records for a specific person ordered by week start descending.
     */
    List<WeeklyAnalytics> findByPersonOrderByWeekStartDesc(Person person);

    /**
     * Find analytics for a specific person and week.
     */
    WeeklyAnalytics findByPersonAndWeekStart(Person person, LocalDate weekStart);

    /**
     * Find all analytics for a specific course and week.
     */
    List<WeeklyAnalytics> findByCourseAndWeekStart(String course, LocalDate weekStart);

    /**
     * Find all analytics for a specific course and week, ordered by week score descending.
     * Used for leaderboard generation.
     */
    List<WeeklyAnalytics> findByCourseAndWeekStartOrderByWeekScoreDesc(String course, LocalDate weekStart);

    /**
     * Find the last N weeks of analytics for a person.
     */
    @Query("SELECT w FROM WeeklyAnalytics w WHERE w.person = :person ORDER BY w.weekStart DESC")
    List<WeeklyAnalytics> findLastNWeeksForPerson(@Param("person") Person person);

    /**
     * Find all active users for a specific course and week.
     */
    @Query("SELECT w FROM WeeklyAnalytics w WHERE w.course = :course AND w.weekStart = :weekStart AND w.isActive = true")
    List<WeeklyAnalytics> findActiveUsersByCourseAndWeek(
        @Param("course") String course,
        @Param("weekStart") LocalDate weekStart
    );

    /**
     * Count active users for a specific course and week.
     */
    @Query("SELECT COUNT(w) FROM WeeklyAnalytics w WHERE w.course = :course AND w.weekStart = :weekStart AND w.isActive = true")
    Long countActiveUsersByCourseAndWeek(
        @Param("course") String course,
        @Param("weekStart") LocalDate weekStart
    );

    /**
     * Find all distinct courses that have analytics data.
     */
    @Query("SELECT DISTINCT w.course FROM WeeklyAnalytics w")
    List<String> findDistinctCourses();

    /**
     * Find all distinct weeks (start dates) that have analytics data.
     */
    @Query("SELECT DISTINCT w.weekStart FROM WeeklyAnalytics w ORDER BY w.weekStart DESC")
    List<LocalDate> findDistinctWeeks();

    /**
     * Get average week score for a course and week.
     */
    @Query("SELECT AVG(w.weekScore) FROM WeeklyAnalytics w WHERE w.course = :course AND w.weekStart = :weekStart AND w.isActive = true")
    Double getAverageWeekScoreByCourseAndWeek(
        @Param("course") String course,
        @Param("weekStart") LocalDate weekStart
    );

    /**
     * Check if analytics already exist for a person and week.
     */
    boolean existsByPersonAndWeekStart(Person person, LocalDate weekStart);

    /**
     * Delete analytics older than a specific date.
     */
    @Query("DELETE FROM WeeklyAnalytics w WHERE w.weekStart < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDate cutoffDate);
}
