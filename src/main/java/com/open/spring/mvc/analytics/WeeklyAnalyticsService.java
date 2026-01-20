package com.open.spring.mvc.analytics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

/**
 * Service for generating and managing weekly analytics.
 * Implements the weekly analytics pipeline described in issue #14.
 */
@Service
public class WeeklyAnalyticsService {

    @Autowired
    private ActivityLogJpaRepository activityLogRepository;

    @Autowired
    private WeeklyAnalyticsJpaRepository weeklyAnalyticsRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    /**
     * Minimum number of events to be considered "active" for a week.
     */
    private static final int ACTIVE_THRESHOLD = 5;

    /**
     * Generate weekly analytics for all active users.
     * Should be run every Sunday at 12:01 AM.
     * 
     * @param weekStart Start date of the week (Monday)
     * @param weekEnd End date of the week (Sunday)
     * @param course Course identifier (optional, null = all courses)
     * @return Number of analytics records generated
     */
    @Transactional
    public int generateWeeklyAnalytics(LocalDate weekStart, LocalDate weekEnd, String course) {
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);

        // Find all persons with activity during the week
        List<Person> activePeople;
        if (course != null) {
            activePeople = activityLogRepository.findDistinctPersonsByCourseAndDateRange(
                    course, startDateTime, endDateTime);
        } else {
            activePeople = activityLogRepository.findDistinctPersonsByDateRange(
                    startDateTime, endDateTime);
        }

        int generatedCount = 0;

        for (Person person : activePeople) {
            // Get activity logs for this person during the week
            List<ActivityLog> logs = activityLogRepository.findByPersonAndDateRange(
                    person, startDateTime, endDateTime);

            // Filter by course if specified
            if (course != null) {
                logs = logs.stream()
                        .filter(log -> course.equals(log.getCourse()))
                        .collect(Collectors.toList());
            }

            // Check if meets active threshold
            if (logs.size() < ACTIVE_THRESHOLD) {
                continue;  // Skip inactive users
            }

            // Determine course (use most common course in logs)
            String personCourse = course != null ? course : getMostCommonCourse(logs);

            // Check if analytics already exist for this person/week/course
            if (weeklyAnalyticsRepository.existsByPersonAndWeekStart(person, weekStart)) {
                continue;  // Skip if already generated
            }

            // Create analytics record
            WeeklyAnalytics analytics = new WeeklyAnalytics();
            analytics.setPerson(person);
            analytics.setCourse(personCourse);
            analytics.setWeekStart(weekStart);
            analytics.setWeekEnd(weekEnd);
            analytics.setGeneratedAt(LocalDateTime.now());
            analytics.setIsActive(true);

            // Calculate school days (default to 5 for now, could integrate with calendar)
            analytics.setSchoolDays(5);

            // Aggregate metrics from logs
            aggregateMetrics(analytics, logs);

            // Calculate derived metrics
            analytics.calculateDerivedMetrics();

            // Save analytics
            weeklyAnalyticsRepository.save(analytics);
            generatedCount++;
        }

        // Calculate percentile ranks and consistency scores
        if (generatedCount > 0) {
            calculatePercentileRanks(weekStart, course);
            calculateConsistencyScores(weekStart, course);
        }

        return generatedCount;
    }

    /**
     * Generate analytics for the previous week.
     * Convenience method for scheduled jobs.
     */
    @Transactional
    public int generateLastWeekAnalytics(String course) {
        LocalDate today = LocalDate.now();
        LocalDate lastSunday = today.with(DayOfWeek.SUNDAY).minusWeeks(1);
        LocalDate lastMonday = lastSunday.with(DayOfWeek.MONDAY);

        return generateWeeklyAnalytics(lastMonday, lastSunday, course);
    }

    /**
     * Aggregate activity metrics from logs into analytics record.
     */
    private void aggregateMetrics(WeeklyAnalytics analytics, List<ActivityLog> logs) {
        Map<String, Integer> eventTypeCounts = new HashMap<>();

        for (ActivityLog log : logs) {
            String eventType = log.getEventType();
            String source = log.getSource();

            // Count by event type
            eventTypeCounts.merge(eventType, 1, Integer::sum);

            // Categorize events
            if ("github".equals(source)) {
                if ("commit".equals(eventType)) {
                    analytics.setCommits(analytics.getCommits() + 1);
                } else if ("pull_request".equals(eventType)) {
                    analytics.setPullRequests(analytics.getPullRequests() + 1);
                } else if ("issue".equals(eventType)) {
                    analytics.setIssues(analytics.getIssues() + 1);
                }
            } else if ("opencs".equals(source)) {
                if ("page_view".equals(eventType)) {
                    analytics.setTotalPageViews(analytics.getTotalPageViews() + 1);
                } else if ("lesson_start".equals(eventType)) {
                    analytics.setLessonsStarted(analytics.getLessonsStarted() + 1);
                } else if ("lesson_complete".equals(eventType)) {
                    analytics.setLessonsCompleted(analytics.getLessonsCompleted() + 1);
                }
            }
        }

        // Calculate unique page views (distinct artifacts with page_view event)
        long uniqueViews = logs.stream()
                .filter(log -> "page_view".equals(log.getEventType()))
                .map(ActivityLog::getArtifact)
                .distinct()
                .count();
        analytics.setUniquePageViews((int) uniqueViews);

        // Store event type breakdown in metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventTypeCounts", eventTypeCounts);
        analytics.setMetadata(metadata);
    }

    /**
     * Calculate percentile ranks for all students in a course/week.
     */
    @Transactional
    public void calculatePercentileRanks(LocalDate weekStart, String course) {
        List<WeeklyAnalytics> allAnalytics;
        
        if (course != null) {
            allAnalytics = weeklyAnalyticsRepository
                    .findByCourseAndWeekStartOrderByWeekScoreDesc(course, weekStart);
        } else {
            // Get all analytics for this week across all courses
            allAnalytics = weeklyAnalyticsRepository.findAll().stream()
                    .filter(w -> w.getWeekStart().equals(weekStart))
                    .sorted((a, b) -> Double.compare(b.getWeekScore(), a.getWeekScore()))
                    .collect(Collectors.toList());
        }

        int totalCount = allAnalytics.size();
        if (totalCount == 0) return;

        for (int i = 0; i < totalCount; i++) {
            WeeklyAnalytics analytics = allAnalytics.get(i);
            // Percentile = (rank / total) * 100, where rank 1 is highest
            double percentile = ((double) (totalCount - i) / totalCount) * 100;
            analytics.setPercentileRank(percentile);
            weeklyAnalyticsRepository.save(analytics);
        }
    }

    /**
     * Calculate consistency scores based on 4-week rolling window.
     * Formula: 0.6 * stability_score + 0.4 * ((trend_score + 1) / 2)
     */
    @Transactional
    public void calculateConsistencyScores(LocalDate weekStart, String course) {
        List<WeeklyAnalytics> currentWeekAnalytics;
        
        if (course != null) {
            currentWeekAnalytics = weeklyAnalyticsRepository.findByCourseAndWeekStart(course, weekStart);
        } else {
            currentWeekAnalytics = weeklyAnalyticsRepository.findAll().stream()
                    .filter(w -> w.getWeekStart().equals(weekStart))
                    .collect(Collectors.toList());
        }

        for (WeeklyAnalytics current : currentWeekAnalytics) {
            // Get last 4 weeks including current
            List<WeeklyAnalytics> last4Weeks = weeklyAnalyticsRepository
                    .findByPersonOrderByWeekStartDesc(current.getPerson())
                    .stream()
                    .limit(4)
                    .collect(Collectors.toList());

            if (last4Weeks.size() < 2) {
                // Not enough data for consistency calculation
                current.setConsistencyScore(null);
                current.setTrendDirection("insufficient_data");
                continue;
            }

            // Calculate mean and std dev of week scores
            double[] scores = last4Weeks.stream()
                    .mapToDouble(WeeklyAnalytics::getWeekScore)
                    .toArray();

            double mean = calculateMean(scores);
            double stdDev = calculateStdDev(scores, mean);

            // Stability score: 1 - (stdDev / mean), clamped to [0, 1]
            double stabilityScore = mean > 0 ? Math.max(0, 1 - (stdDev / mean)) : 0;

            // Trend score using linear regression slope, normalized to [-1, 1]
            double trendSlope = calculateTrendSlope(last4Weeks);
            double trendScore = Math.max(-1, Math.min(1, trendSlope / 10));  // Normalize

            // Consistency score: 0.6 * stability + 0.4 * ((trend + 1) / 2)
            double consistencyScore = 0.6 * stabilityScore + 0.4 * ((trendScore + 1) / 2);
            current.setConsistencyScore(consistencyScore);

            // Determine trend direction
            if (last4Weeks.size() >= 2) {
                double currentScore = last4Weeks.get(0).getWeekScore();
                double previousScore = last4Weeks.get(1).getWeekScore();
                double change = currentScore - previousScore;

                if (change > 5) {
                    current.setTrendDirection("increasing");
                } else if (change < -5) {
                    current.setTrendDirection("declining");
                } else {
                    current.setTrendDirection("stable");
                }
            }

            weeklyAnalyticsRepository.save(current);
        }
    }

    /**
     * Get most common course from logs.
     */
    private String getMostCommonCourse(List<ActivityLog> logs) {
        Map<String, Long> courseCounts = logs.stream()
                .collect(Collectors.groupingBy(ActivityLog::getCourse, Collectors.counting()));

        return courseCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }

    private double calculateMean(double[] values) {
        if (values.length == 0) return 0;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private double calculateStdDev(double[] values, double mean) {
        if (values.length <= 1) return 0;
        double sumSquares = 0;
        for (double v : values) {
            sumSquares += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSquares / (values.length - 1));
    }

    private double calculateTrendSlope(List<WeeklyAnalytics> weeks) {
        if (weeks.size() < 2) return 0;

        List<WeeklyAnalytics> reversed = new ArrayList<>(weeks);
        java.util.Collections.reverse(reversed);  // Oldest to newest

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = reversed.size();

        for (int i = 0; i < n; i++) {
            double x = i;  // Week index
            double y = reversed.get(i).getWeekScore();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // Linear regression slope: (n*sumXY - sumX*sumY) / (n*sumX2 - sumX^2)
        double denominator = (n * sumX2 - sumX * sumX);
        if (denominator == 0) return 0;
        
        return (n * sumXY - sumX * sumY) / denominator;
    }
}
