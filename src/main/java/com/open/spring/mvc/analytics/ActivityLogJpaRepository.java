package com.open.spring.mvc.analytics;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.open.spring.mvc.person.Person;

/**
 * JPA Repository for ActivityLog entity.
 * Provides database access methods for querying activity logs.
 */
public interface ActivityLogJpaRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Find all activity logs for a specific person.
     */
    List<ActivityLog> findByPerson(Person person);

    /**
     * Find all activity logs for a specific person ordered by timestamp descending.
     */
    List<ActivityLog> findByPersonOrderByTimestampDesc(Person person);

    /**
     * Find activity logs for a person within a date range.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.person = :person AND a.timestamp BETWEEN :startDate AND :endDate")
    List<ActivityLog> findByPersonAndDateRange(
        @Param("person") Person person,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find activity logs by course within a date range.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.course = :course AND a.timestamp BETWEEN :startDate AND :endDate")
    List<ActivityLog> findByCourseAndDateRange(
        @Param("course") String course,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find activity logs by source (opencs, github, calendar).
     */
    List<ActivityLog> findBySource(String source);

    /**
     * Find activity logs by event type.
     */
    List<ActivityLog> findByEventType(String eventType);

    /**
     * Count events for a person within a date range.
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.person = :person AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countByPersonAndDateRange(
        @Param("person") Person person,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get sum of event weights for a person within a date range.
     */
    @Query("SELECT COALESCE(SUM(a.eventWeight), 0) FROM ActivityLog a WHERE a.person = :person AND a.timestamp BETWEEN :startDate AND :endDate")
    Double sumEventWeightsByPersonAndDateRange(
        @Param("person") Person person,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count events by event type for a person within a date range.
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.person = :person AND a.eventType = :eventType AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countByPersonEventTypeAndDateRange(
        @Param("person") Person person,
        @Param("eventType") String eventType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count events by source for a person within a date range.
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.person = :person AND a.source = :source AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countByPersonSourceAndDateRange(
        @Param("person") Person person,
        @Param("source") String source,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all distinct persons who have activity in a date range.
     */
    @Query("SELECT DISTINCT a.person FROM ActivityLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    List<Person> findDistinctPersonsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all distinct persons who have activity in a date range for a specific course.
     */
    @Query("SELECT DISTINCT a.person FROM ActivityLog a WHERE a.course = :course AND a.timestamp BETWEEN :startDate AND :endDate")
    List<Person> findDistinctPersonsByCourseAndDateRange(
        @Param("course") String course,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Delete activity logs older than a specific date.
     */
    @Query("DELETE FROM ActivityLog a WHERE a.timestamp < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
