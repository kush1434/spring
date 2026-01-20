package com.open.spring.mvc.analytics;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

import lombok.Data;

/**
 * REST API Controller for Activity Logging.
 * Provides endpoints to log student activities and retrieve activity data.
 */
@RestController
@RequestMapping("/api/activity")
@CrossOrigin(origins = "*")
public class ActivityLogApiController {

    @Autowired
    private ActivityLogJpaRepository activityLogRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    /**
     * DTO for logging activity events.
     */
    @Data
    public static class ActivityLogRequest {
        private String githubLogin;  // Can be null if using authenticated user
        private String source;       // opencs, github, calendar
        private String course;       // CSA, CSP, CSSE, etc.
        private String artifact;     // Lesson name, repo name, etc.
        private String eventType;    // page_view, commit, issue, lesson_complete, etc.
        private Double eventWeight;  // Optional, defaults to 1.0
        private Map<String, Object> metadata;  // Optional additional data
    }

    /**
     * Log a single activity event.
     * Can be called by the user themselves or by an admin for any user.
     * 
     * POST /api/activity/log
     * Body: ActivityLogRequest JSON
     */
    @PostMapping("/log")
    public ResponseEntity<?> logActivity(
            @RequestBody ActivityLogRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Person person;

            // If githubLogin is provided, look up that user (requires admin for other users)
            if (request.getGithubLogin() != null && !request.getGithubLogin().isEmpty()) {
                person = personRepository.findByUid(request.getGithubLogin());
                if (person == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found: " + request.getGithubLogin()));
                }

                // Check if logging for someone else (requires admin)
                if (userDetails != null && !userDetails.getUsername().equals(request.getGithubLogin())) {
                    // Would need to check if current user is admin
                    // For now, allow it but could add authorization check
                }
            } else {
                // Use authenticated user
                if (userDetails == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Authentication required"));
                }
                person = personRepository.findByEmail(userDetails.getUsername());
                if (person == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Authenticated user not found"));
                }
            }

            // Validate required fields
            if (request.getSource() == null || request.getSource().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Source is required"));
            }
            if (request.getCourse() == null || request.getCourse().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Course is required"));
            }
            if (request.getEventType() == null || request.getEventType().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Event type is required"));
            }

            // Create and save activity log
            ActivityLog log = new ActivityLog();
            log.setPerson(person);
            log.setSource(request.getSource());
            log.setCourse(request.getCourse());
            log.setArtifact(request.getArtifact());
            log.setEventType(request.getEventType());
            log.setEventWeight(request.getEventWeight() != null ? request.getEventWeight() : 1.0);
            log.setMetadata(request.getMetadata());
            log.setTimestamp(LocalDateTime.now());

            activityLogRepository.save(log);

            return ResponseEntity.status(HttpStatus.CREATED).body(log);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to log activity: " + e.getMessage()));
        }
    }

    /**
     * Bulk log multiple activity events.
     * Useful for batch imports or syncing from external systems.
     * 
     * POST /api/activity/log/bulk
     * Body: List of ActivityLogRequest JSON
     */
    @PostMapping("/log/bulk")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> logActivitiesBulk(
            @RequestBody List<ActivityLogRequest> requests,
            Authentication authentication) {

        try {
            int successCount = 0;
            int failCount = 0;

            for (ActivityLogRequest request : requests) {
                try {
                    Person person = personRepository.findByUid(request.getGithubLogin());
                    if (person == null) {
                        failCount++;
                        continue;
                    }

                    ActivityLog log = new ActivityLog();
                    log.setPerson(person);
                    log.setSource(request.getSource());
                    log.setCourse(request.getCourse());
                    log.setArtifact(request.getArtifact());
                    log.setEventType(request.getEventType());
                    log.setEventWeight(request.getEventWeight() != null ? request.getEventWeight() : 1.0);
                    log.setMetadata(request.getMetadata());
                    log.setTimestamp(LocalDateTime.now());

                    activityLogRepository.save(log);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", successCount,
                    "failed", failCount,
                    "total", requests.size()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to bulk log activities: " + e.getMessage()));
        }
    }

    /**
     * Get activity logs for the authenticated user.
     * 
     * GET /api/activity/my-logs
     */
    @GetMapping("/my-logs")
    public ResponseEntity<?> getMyLogs(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            Person person = personRepository.findByEmail(userDetails.getUsername());
            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            List<ActivityLog> logs = activityLogRepository.findByPersonOrderByTimestampDesc(person);
            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve logs: " + e.getMessage()));
        }
    }

    /**
     * Get activity logs for a specific user by github login.
     * Requires admin/teacher role.
     * 
     * GET /api/activity/user/{githubLogin}
     */
    @GetMapping("/user/{githubLogin}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> getUserLogs(@PathVariable String githubLogin) {
        try {
            Person person = personRepository.findByUid(githubLogin);
            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            List<ActivityLog> logs = activityLogRepository.findByPersonOrderByTimestampDesc(person);
            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve logs: " + e.getMessage()));
        }
    }

    /**
     * Get activity logs for a date range.
     * Requires admin/teacher role.
     * 
     * GET /api/activity/range?start=2026-01-01T00:00:00&end=2026-01-07T23:59:59&course=CSA
     */
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> getLogsInRange(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String course) {

        try {
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate = LocalDateTime.parse(end);

            List<ActivityLog> logs;
            if (course != null && !course.isEmpty()) {
                logs = activityLogRepository.findByCourseAndDateRange(course, startDate, endDate);
            } else {
                // Would need a method to get all logs in range
                logs = activityLogRepository.findAll().stream()
                        .filter(log -> !log.getTimestamp().isBefore(startDate) && 
                                       !log.getTimestamp().isAfter(endDate))
                        .toList();
            }

            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve logs: " + e.getMessage()));
        }
    }

    /**
     * Get activity summary for current user.
     * Returns counts by event type and source.
     * 
     * GET /api/activity/my-summary
     */
    @GetMapping("/my-summary")
    public ResponseEntity<?> getMySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }

            Person person = personRepository.findByEmail(userDetails.getUsername());
            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            LocalDateTime startDate = start != null ? LocalDateTime.parse(start) : LocalDateTime.now().minusWeeks(1);
            LocalDateTime endDate = end != null ? LocalDateTime.parse(end) : LocalDateTime.now();

            List<ActivityLog> logs = activityLogRepository.findByPersonAndDateRange(person, startDate, endDate);

            // Build summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalEvents", logs.size());
            summary.put("totalWeight", logs.stream().mapToDouble(ActivityLog::getEventWeight).sum());

            // Count by event type
            Map<String, Long> byEventType = new HashMap<>();
            logs.forEach(log -> byEventType.merge(log.getEventType(), 1L, Long::sum));
            summary.put("byEventType", byEventType);

            // Count by source
            Map<String, Long> bySource = new HashMap<>();
            logs.forEach(log -> bySource.merge(log.getSource(), 1L, Long::sum));
            summary.put("bySource", bySource);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate summary: " + e.getMessage()));
        }
    }

    /**
     * Delete old activity logs (admin only).
     * Removes logs older than specified date.
     * 
     * DELETE /api/activity/cleanup?before=2025-01-01T00:00:00
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cleanupOldLogs(@RequestParam String before) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.parse(before);
            activityLogRepository.deleteOlderThan(cutoffDate);
            return ResponseEntity.ok(Map.of("message", "Cleanup completed", "cutoffDate", cutoffDate));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cleanup logs: " + e.getMessage()));
        }
    }
}
