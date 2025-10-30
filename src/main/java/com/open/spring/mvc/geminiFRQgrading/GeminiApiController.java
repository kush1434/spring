package com.open.spring.mvc.geminiFRQgrading;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeminiApiController {

    private final Dotenv dotenv = Dotenv.load();
    private final String geminiApiKey = dotenv.get("GEMINI_API_KEY");
    private final String geminiApiUrl = dotenv.get("GEMINI_API_URL");

    @PostMapping("/grade")
    public ResponseEntity<?> grade(@RequestBody Map<String, String> body) {
        try {
            String question = body.get("question");
            String answer = body.get("answer");

            if (question == null || answer == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing question or answer"));
            }

            // Build the grading prompt
            String prompt = String.format("""
                You are an expert tutor grading a student's answer to a free-response question about Jekyll themes.
                Your task is to:
                1. Determine a grade for the student's response based on the following 1–5 scale:
                   - 5: The answer addresses all parts of the question and is detailed and comprehensive.
                   - 4: The answer is correct and addresses most parts of the question.
                   - 3: The answer is correct but may be incomplete or lack detail.
                   - 2: The answer has significant inaccuracies or is incomplete.
                   - 1: The answer is incorrect or does not address the question.
                   Write the grade like this: "Grade: (1-5)/5"
                2. Provide detailed, constructive feedback explaining the grade.
                3. Offer very short suggestions on what the user could improve on.
                The question is: %s
                The student's response is: %s
                Format your final output with a clear heading for the grade and feedback. Also, do not use HTML or markdown formatting in your reposnse, just simple text.
            """, question, answer);

            // Proper JSON payload with escaped characters
            String jsonPayload = String.format("""
                {
                    "contents": [{
                        "parts": [{
                            "text": "%s"
                        }]
                    }]
                }
            """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

            // Send POST request to Gemini API
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.POST, request, String.class);

            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException.TooManyRequests e) {
            return ResponseEntity.status(429).body(Map.of("error", "Gemini quota exceeded. Please try again later."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}

