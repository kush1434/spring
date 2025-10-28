package com.open.spring.mvc.grades;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/grades")
public class GradesController {

    @Autowired
    private GradesRepository gradesRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeRequest {
        private String uid;
        private String question;
        private String response;
    }

    // Grade a single response and persist result
    @PostMapping("/grade")
    public ResponseEntity<?> grade(@RequestBody GradeRequest req) {
        if (req == null || req.getUid() == null || req.getQuestion() == null || req.getResponse() == null) {
            return new ResponseEntity<>(Map.of("error", "uid, question and response are required"), HttpStatus.BAD_REQUEST);
        }

        double gradeValue = computeGrade(req.getQuestion(), req.getResponse());
        gradeValue = clamp(gradeValue, 0.55, 1.0);

        Grades entry = new Grades(req.getUid(), req.getQuestion(), req.getResponse(), gradeValue);
        entry.setCreatedAt(LocalDateTime.now());
        gradesRepository.save(entry);

        return new ResponseEntity<>(
                Map.of(
                        "status", "success",
                        "uid", req.getUid(),
                        "grade", gradeValue,
                        "createdAt", entry.getCreatedAt()
                ),
                HttpStatus.OK
        );
    }

    // Get all grades for a user (newest first)
    @GetMapping("/user/{uid}")
    public ResponseEntity<?> getUserGrades(@PathVariable String uid) {
        List<Grades> list = gradesRepository.findByUidOrderByCreatedAtDesc(uid);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // --- internal helpers ---

    private double computeGrade(String question, String response) {
        // Try external grader using env vars; fallback to local heuristic if unavailable or on error.
        String apiKey = System.getenv("GAMIFY_API_KEY");
        String apiUrl = System.getenv("GAMIFY_API_URL");

        if (apiKey != null && !apiKey.isBlank() && apiUrl != null && !apiUrl.isBlank()) {
            try {
                Double remote = callExternalGrader(apiUrl, apiKey, question, response);
                if (remote != null) {
                    return clamp(remote, 0.55, 1.0);
                }
            } catch (Exception e) {
                // fallback to local
            }
        }

        return clamp(localHeuristicGrade(question, response), 0.55, 1.0);
    }

    private Double callExternalGrader(String apiUrl, String apiKey, String question, String response) throws Exception {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Build a compact chat-style payload asking for a single numeric grade between 0.55 and 1.0
        // Note: Adjust model/fields as needed for your target API. This payload works with many chat-like endpoints.
        Map<String, Object> message = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a grader. Return a single numeric grade between 0.55 and 1.0."),
                        Map.of("role", "user", "content", "Question: " + question + "\n\nResponse: " + response + "\n\nReturn only a numeric grade between 0.55 and 1.0.")
                ),
                "max_tokens", 10
        );

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(message);

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp = rest.postForEntity(apiUrl, entity, String.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            String body = resp.getBody();

            // Try to parse common chat-completion shapes: choices[0].message.content OR choices[0].text
            try {
                JsonNode root = mapper.readTree(body);
                if (root.has("choices") && root.get("choices").size() > 0) {
                    JsonNode first = root.get("choices").get(0);
                    String content = null;
                    if (first.has("message") && first.get("message").has("content")) {
                        content = first.get("message").get("content").asText();
                    } else if (first.has("text")) {
                        content = first.get("text").asText();
                    }
                    if (content != null) {
                        Double parsed = extractFirstNumber(content);
                        if (parsed != null) return parsed;
                    }
                }
            } catch (Exception ignore) {
            }

            // As a fallback, attempt to extract a number anywhere in the raw body.
            Double parsedRaw = extractFirstNumber(body);
            if (parsedRaw != null) return parsedRaw;
        }

        return null;
    }

    private Double extractFirstNumber(String text) {
        if (text == null) return null;
        Pattern p = Pattern.compile("([0-9]+\\.[0-9]+)|([0-9]+)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private double localHeuristicGrade(String question, String response) {
        // Simple word-overlap heuristic mapped into [0.55, 1.0]
        if (question == null || question.isBlank()) return 0.55;
        String[] qWords = question.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        String[] rWords = response == null ? new String[0] : response.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");

        Set<String> qSet = new HashSet<>();
        for (String w : qWords) if (!w.isBlank()) qSet.add(w);
        Set<String> rSet = new HashSet<>();
        for (String w : rWords) if (!w.isBlank()) rSet.add(w);

        if (qSet.isEmpty()) return 0.55;
        int common = 0;
        for (String w : qSet) if (rSet.contains(w)) common++;

        double ratio = (double) common / (double) qSet.size(); // 0..1
        return 0.55 + 0.45 * ratio; // maps 0->0.55, 1->1.0
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
