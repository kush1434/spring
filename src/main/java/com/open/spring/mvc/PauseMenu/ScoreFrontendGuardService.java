package com.open.spring.mvc.PauseMenu;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ScoreFrontendGuardService {

    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(2);

    private static final Set<String> ALLOWED_LOCAL_ORIGINS = Set.of(
            "http://localhost:8585",
            "http://127.0.0.1:8585",
            "http://localhost:4600",
            "http://127.0.0.1:4600",
            "http://localhost:4599",
            "http://127.0.0.1:4599",
            "http://localhost:4500",
            "http://127.0.0.1:4500",
            "https://pages.opencodingsociety.com"
    );

    private final ConcurrentMap<String, ChallengeRecord> challengeStore = new ConcurrentHashMap<>();

    public Map<String, Object> issueChallenge(HttpServletRequest request) {
        if (!isBrowserContext(request)) {
            return Map.of("ok", false, "message", "Browser context required");
        }

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(CHALLENGE_TTL);
        challengeStore.put(token, new ChallengeRecord(fingerprint(request), expiresAt));

        return Map.of(
                "ok", true,
                "challengeToken", token,
                "expiresAt", expiresAt.toString());
    }

    public boolean validateAndConsume(HttpServletRequest request, String token) {
        if (token == null || token.isBlank() || !isBrowserContext(request)) {
            return false;
        }

        ChallengeRecord challenge = challengeStore.remove(token);
        if (challenge == null) {
            return false;
        }

        if (challenge.expiresAt.isBefore(Instant.now())) {
            return false;
        }

        return challenge.fingerprint.equals(fingerprint(request));
    }

    private boolean isBrowserContext(HttpServletRequest request) {
        String secFetchMode = request.getHeader("Sec-Fetch-Mode");
        String secFetchSite = request.getHeader("Sec-Fetch-Site");

        if (secFetchMode == null || secFetchSite == null) {
            return false;
        }

        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        if (origin != null && !origin.isBlank()) {
            return isAllowedOrigin(origin);
        }

        if (referer != null && !referer.isBlank()) {
            return isAllowedOrigin(referer);
        }

        return false;
    }

    private boolean isAllowedOrigin(String originOrReferer) {
        try {
            URI uri = URI.create(originOrReferer);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            if (scheme == null || host == null) {
                return false;
            }

            String normalizedOrigin = scheme + "://" + host + (port > 0 ? ":" + port : "");
            if (ALLOWED_LOCAL_ORIGINS.contains(normalizedOrigin)) {
                return true;
            }

            if ("https".equalsIgnoreCase(scheme)) {
                if ("open-coding-society.github.io".equalsIgnoreCase(host)) {
                    return true;
                }
                return host.endsWith(".opencodingsociety.com");
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private String fingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            userAgent = "unknown";
        }
        return request.getRemoteAddr() + "|" + userAgent;
    }

    private record ChallengeRecord(String fingerprint, Instant expiresAt) {
    }
}
