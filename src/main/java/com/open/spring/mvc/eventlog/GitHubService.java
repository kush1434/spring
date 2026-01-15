package com.open.spring.mvc.eventlog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for integrating with GitHub API to fetch user events.
 * Fetches commits (PushEvent), pull requests (PullRequestEvent), and issues (IssuesEvent).
 */
@Slf4j
@Service
public class GitHubService {

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    @Value("${github.api.token:}")
    private String githubApiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch GitHub events for a user and convert them to UserEvent objects
     * @param githubLogin the GitHub username
     * @param course the course identifier (e.g., "CSA")
     * @return list of UserEvent objects parsed from GitHub API
     */
    public List<UserEvent> fetchGitHubEventsForUser(String githubLogin, String course) {
        List<UserEvent> events = new ArrayList<>();

        try {
            String url = String.format("%s/users/%s/events/public", githubApiBaseUrl, githubLogin);
            log.info("Fetching GitHub events for user: {} from URL: {}", githubLogin, url);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Add authentication token if available
            if (githubApiToken != null && !githubApiToken.isEmpty()) {
                headers.setBearerAuth(githubApiToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode eventsArray = objectMapper.readTree(response.getBody());

                for (JsonNode eventNode : eventsArray) {
                    UserEvent userEvent = parseGitHubEvent(eventNode, githubLogin, course);
                    if (userEvent != null) {
                        events.add(userEvent);
                    }
                }

                log.info("Successfully fetched {} events for user: {}", events.size(), githubLogin);
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.error("GitHub user not found: {}", githubLogin);
            throw new RuntimeException("GitHub user not found: " + githubLogin);
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("GitHub API rate limit exceeded or access forbidden");
            throw new RuntimeException("GitHub API rate limit exceeded. Please try again later.");
        } catch (Exception e) {
            log.error("Error fetching GitHub events for user {}: {}", githubLogin, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch GitHub events: " + e.getMessage());
        }

        return events;
    }

    /**
     * Parse a single GitHub event JSON node into a UserEvent object
     * @param eventNode the JSON node from GitHub API
     * @param githubLogin the GitHub username
     * @param course the course identifier
     * @return UserEvent object or null if event type is not relevant
     */
    private UserEvent parseGitHubEvent(JsonNode eventNode, String githubLogin, String course) {
        try {
            String eventType = eventNode.get("type").asText();
            String createdAt = eventNode.get("created_at").asText();
            String repoName = eventNode.get("repo").get("name").asText();

            // Convert ISO 8601 timestamp to LocalDateTime
            LocalDateTime timestamp = ZonedDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
                .toLocalDateTime();

            UserEvent userEvent = new UserEvent();
            userEvent.setGithubLogin(githubLogin);
            userEvent.setSource("github");
            userEvent.setCourse(course);
            userEvent.setArtifact(repoName);
            userEvent.setTimestamp(timestamp);

            // Map GitHub event types to our event types with appropriate weights
            switch (eventType) {
                case "PushEvent":
                    // Count the number of commits in the push
                    JsonNode commits = eventNode.get("payload").get("commits");
                    int commitCount = commits != null ? commits.size() : 1;

                    // Create an event for the push (we could create separate events per commit)
                    userEvent.setEventType("commit");
                    userEvent.setEventWeight(3.0 * commitCount);  // 3.0 per commit
                    return userEvent;

                case "PullRequestEvent":
                    String action = eventNode.get("payload").get("action").asText();
                    // Only count when PR is opened (not closed, synchronized, etc.)
                    if ("opened".equals(action)) {
                        userEvent.setEventType("pr");
                        userEvent.setEventWeight(4.0);
                        return userEvent;
                    }
                    return null;  // Ignore other PR actions

                case "IssuesEvent":
                    String issueAction = eventNode.get("payload").get("action").asText();
                    // Only count when issue is opened
                    if ("opened".equals(issueAction)) {
                        userEvent.setEventType("issue");
                        userEvent.setEventWeight(3.0);
                        return userEvent;
                    }
                    return null;  // Ignore other issue actions

                default:
                    // Ignore other event types (ForkEvent, WatchEvent, etc.)
                    return null;
            }

        } catch (Exception e) {
            log.error("Error parsing GitHub event: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get the rate limit status from GitHub API
     * @return JSON string with rate limit information
     */
    public String getRateLimitStatus() {
        try {
            String url = githubApiBaseUrl + "/rate_limit";
            HttpHeaders headers = new HttpHeaders();

            if (githubApiToken != null && !githubApiToken.isEmpty()) {
                headers.setBearerAuth(githubApiToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching rate limit status: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
