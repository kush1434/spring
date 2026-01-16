package com.open.spring.mvc.analytics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

import lombok.Data;

/**
 * REST API Controller for Weekly Analytics and Leaderboards.
 * Provides endpoints for viewing analytics data and generating weekly reports.
 */
@RestController
@RequestMapping("/api/weekly-analytics")
@CrossOrigin(origins = "*")
public class WeeklyAnalyticsApiController {

    @Autowired
    private WeeklyAnalyticsJpaRepository weeklyAnalyticsRepository;

    @Autowired
    private WeeklyAnalyticsService weeklyAnalyticsService;

    @Autowired
    private PersonJpaRepository personRepository;

    /**
     * DTO for leaderboard entry.
     */
    @Data
    public static class LeaderboardEntry {
        private String name;           // Student name (for teacher/admin view)
        private String identifier;     // Partial identifier for student view
        private Double weekScore;
        private Double percentileRank;
        private Integer commits;
        private Integer pullRequests;
        private Integer issues;
        private Integer totalPageViews;
        private Integer lessonsCompleted;
        private String trendDirection;
        private Boolean isCurrentUser;
    }

    /**
     * DTO for analytics summary.
     */
    @Data
    public static class AnalyticsSummary {
        private String course;
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private Integer totalActiveUsers;
        private Double averageWeekScore;
        private Double myWeekScore;
        private Double myPercentileRank;
        private String myTrendDirection;
        private WeeklyAnalytics myAnalytics;
    }

    /**
     * Generate weekly analytics (admin/teacher only).
     * Should be scheduled to run every Sunday at 12:01 AM.
     * 
     * POST /api/weekly-analytics/generate?course=CSA
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> generateWeeklyAnalytics(
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String weekStart) {

        try {
            LocalDate start;
            LocalDate end;

            if (weekStart != null) {
                start = LocalDate.parse(weekStart);
                end = start.plusDays(6);
            } else {
                // Use last week
                LocalDate today = LocalDate.now();
                LocalDate lastSunday = today.with(DayOfWeek.SUNDAY).minusWeeks(1);
                start = lastSunday.with(DayOfWeek.MONDAY);
                end = lastSunday;
            }

            int count = weeklyAnalyticsService.generateWeeklyAnalytics(start, end, course);

            return ResponseEntity.ok(Map.of(
                    "message", "Weekly analytics generated",
                    "weekStart", start,
                    "weekEnd", end,
                    "course", course != null ? course : "all",
                    "recordsGenerated", count));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate analytics: " + e.getMessage()));
        }
    }

    /**
     * Get my weekly analytics.
     * Returns the current user's analytics for a specific week.
     * 
     * GET /api/weekly-analytics/my-analytics?weekStart=2026-01-13
     */
    @GetMapping("/my-analytics")
    public ResponseEntity<?> getMyAnalytics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String weekStart) {

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

            LocalDate week;
            if (weekStart != null) {
                week = LocalDate.parse(weekStart);
            } else {
                // Use last Monday
                week = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
            }

            WeeklyAnalytics analytics = weeklyAnalyticsRepository.findByPersonAndWeekStart(person, week);

            if (analytics == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No analytics found for this week"));
            }

            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve analytics: " + e.getMessage()));
        }
    }

    /**
     * Get my weekly history (last 4 weeks).
     * 
     * GET /api/weekly-analytics/my-history
     */
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyHistory(@AuthenticationPrincipal UserDetails userDetails) {
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

            List<WeeklyAnalytics> history = weeklyAnalyticsRepository
                    .findByPersonOrderByWeekStartDesc(person)
                    .stream()
                    .limit(4)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve history: " + e.getMessage()));
        }
    }

    /**
     * Get course leaderboard for a specific week.
     * 
     * Students see:
     * - Their own position and percentile
     * - Aggregated distribution (no other names)
     * 
     * Teachers/Admins see:
     * - Full leaderboard with all names
     * - Detailed metrics for each student
     * 
     * GET /api/weekly-analytics/leaderboard?course=CSA&weekStart=2026-01-13
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String course,
            @RequestParam(required = false) String weekStart,
            Authentication authentication) {

        try {
            LocalDate week;
            if (weekStart != null) {
                week = LocalDate.parse(weekStart);
            } else {
                week = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
            }

            List<WeeklyAnalytics> allAnalytics = weeklyAnalyticsRepository
                    .findByCourseAndWeekStartOrderByWeekScoreDesc(course, week);

            if (allAnalytics.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No analytics found for this course and week"));
            }

            // Check if user is admin/teacher
            boolean isAdmin = authentication != null && 
                             (authentication.getAuthorities().stream()
                                     .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                                    a.getAuthority().equals("ROLE_TEACHER")));

            Person currentUser = null;
            if (userDetails != null) {
                currentUser = personRepository.findByEmail(userDetails.getUsername());
            }

            if (isAdmin) {
                // Full leaderboard for teachers/admins
                List<LeaderboardEntry> leaderboard = new ArrayList<>();
                
                for (WeeklyAnalytics analytics : allAnalytics) {
                    LeaderboardEntry entry = new LeaderboardEntry();
                    entry.setName(analytics.getPerson().getName());
                    entry.setIdentifier(analytics.getPerson().getUid());
                    entry.setWeekScore(analytics.getWeekScore());
                    entry.setPercentileRank(analytics.getPercentileRank());
                    entry.setCommits(analytics.getCommits());
                    entry.setPullRequests(analytics.getPullRequests());
                    entry.setIssues(analytics.getIssues());
                    entry.setTotalPageViews(analytics.getTotalPageViews());
                    entry.setLessonsCompleted(analytics.getLessonsCompleted());
                    entry.setTrendDirection(analytics.getTrendDirection());
                    entry.setIsCurrentUser(currentUser != null && 
                                          analytics.getPerson().getId().equals(currentUser.getId()));
                    leaderboard.add(entry);
                }

                return ResponseEntity.ok(Map.of(
                        "type", "full",
                        "course", course,
                        "weekStart", week,
                        "leaderboard", leaderboard));

            } else {
                // Limited view for students
                if (currentUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Authentication required"));
                }

                WeeklyAnalytics myAnalytics = allAnalytics.stream()
                        .filter(a -> a.getPerson().getId().equals(currentUser.getId()))
                        .findFirst()
                        .orElse(null);

                if (myAnalytics == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "You don't have analytics for this week"));
                }

                // Calculate quartiles for context
                List<Double> scores = allAnalytics.stream()
                        .map(WeeklyAnalytics::getWeekScore)
                        .sorted()
                        .collect(Collectors.toList());

                Map<String, Object> distribution = new HashMap<>();
                distribution.put("totalStudents", allAnalytics.size());
                distribution.put("q1", getPercentile(scores, 25));
                distribution.put("median", getPercentile(scores, 50));
                distribution.put("q3", getPercentile(scores, 75));
                distribution.put("max", scores.get(scores.size() - 1));
                distribution.put("min", scores.get(0));

                Map<String, Object> myPosition = new HashMap<>();
                myPosition.put("weekScore", myAnalytics.getWeekScore());
                myPosition.put("percentileRank", myAnalytics.getPercentileRank());
                myPosition.put("trendDirection", myAnalytics.getTrendDirection());
                myPosition.put("commits", myAnalytics.getCommits());
                myPosition.put("pullRequests", myAnalytics.getPullRequests());
                myPosition.put("issues", myAnalytics.getIssues());
                myPosition.put("pageViews", myAnalytics.getTotalPageViews());
                myPosition.put("lessonsCompleted", myAnalytics.getLessonsCompleted());

                return ResponseEntity.ok(Map.of(
                        "type", "student",
                        "course", course,
                        "weekStart", week,
                        "myPosition", myPosition,
                        "distribution", distribution));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve leaderboard: " + e.getMessage()));
        }
    }

    /**
     * Get all available weeks with analytics data.
     * 
     * GET /api/weekly-analytics/weeks
     */
    @GetMapping("/weeks")
    public ResponseEntity<?> getAvailableWeeks() {
        try {
            List<LocalDate> weeks = weeklyAnalyticsRepository.findDistinctWeeks();
            return ResponseEntity.ok(weeks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve weeks: " + e.getMessage()));
        }
    }

    /**
     * Get all available courses with analytics data.
     * 
     * GET /api/weekly-analytics/courses
     */
    @GetMapping("/courses")
    public ResponseEntity<?> getAvailableCourses() {
        try {
            List<String> courses = weeklyAnalyticsRepository.findDistinctCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve courses: " + e.getMessage()));
        }
    }

    /**
     * Get analytics for a specific user (admin/teacher only).
     * 
     * GET /api/weekly-analytics/user/{githubLogin}?weekStart=2026-01-13
     */
    @GetMapping("/user/{githubLogin}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> getUserAnalytics(
            @PathVariable String githubLogin,
            @RequestParam(required = false) String weekStart) {

        try {
            Person person = personRepository.findByUid(githubLogin);
            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            if (weekStart != null) {
                LocalDate week = LocalDate.parse(weekStart);
                WeeklyAnalytics analytics = weeklyAnalyticsRepository.findByPersonAndWeekStart(person, week);
                
                if (analytics == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "No analytics found for this week"));
                }
                
                return ResponseEntity.ok(analytics);
            } else {
                // Return last 4 weeks
                List<WeeklyAnalytics> history = weeklyAnalyticsRepository
                        .findByPersonOrderByWeekStartDesc(person)
                        .stream()
                        .limit(4)
                        .collect(Collectors.toList());
                
                return ResponseEntity.ok(history);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve analytics: " + e.getMessage()));
        }
    }

    /**
     * Get course summary for a specific week (admin/teacher only).
     * 
     * GET /api/weekly-analytics/course-summary?course=CSA&weekStart=2026-01-13
     */
    @GetMapping("/course-summary")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> getCourseSummary(
            @RequestParam String course,
            @RequestParam(required = false) String weekStart) {

        try {
            LocalDate week = weekStart != null ? LocalDate.parse(weekStart) : 
                            LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);

            List<WeeklyAnalytics> allAnalytics = weeklyAnalyticsRepository
                    .findByCourseAndWeekStart(course, week);

            if (allAnalytics.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No analytics found"));
            }

            Long activeCount = weeklyAnalyticsRepository.countActiveUsersByCourseAndWeek(course, week);
            Double avgScore = weeklyAnalyticsRepository.getAverageWeekScoreByCourseAndWeek(course, week);

            Map<String, Object> summary = new HashMap<>();
            summary.put("course", course);
            summary.put("weekStart", week);
            summary.put("totalStudents", allAnalytics.size());
            summary.put("activeStudents", activeCount);
            summary.put("averageWeekScore", avgScore);

            // Calculate trend distribution
            Map<String, Long> trendCounts = allAnalytics.stream()
                    .filter(a -> a.getTrendDirection() != null)
                    .collect(Collectors.groupingBy(
                            WeeklyAnalytics::getTrendDirection,
                            Collectors.counting()));
            summary.put("trendDistribution", trendCounts);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve summary: " + e.getMessage()));
        }
    }

    /**
     * Calculate percentile for a sorted list.
     */
    private double getPercentile(List<Double> sortedScores, int percentile) {
        if (sortedScores.isEmpty()) return 0.0;
        
        int index = (int) Math.ceil(percentile / 100.0 * sortedScores.size()) - 1;
        index = Math.max(0, Math.min(index, sortedScores.size() - 1));
        
        return sortedScores.get(index);
    }
}
