package com.open.spring.mvc.automaticFRQFeedback;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class feedbackController {
    
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiFeedbackService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public FeedbackResponse generateFeedback(FRQMetadata frqData, String studentResponse, String rubric) {
        try {
            String prompt = buildPrompt(frqData, studentResponse, rubric);
            String geminiResponse = callGeminiAPI(prompt);
            return parseAndValidate(geminiResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate feedback from Gemini API", e);
        }
    }

    private String buildPrompt(FRQMetadata frqData, String studentResponse, String rubric) {
        return String.format("""
            You are an AP exam grader. Evaluate the following student response and provide structured feedback.
            
            **FRQ Metadata:**
            - Year: %s
            - Question Number: %d
            - Topic: %s
            
            **Student Response:**
            %s
            
            **Scoring Rubric:**
            %s
            
            **Instructions:**
            Provide your evaluation in the following JSON format (respond with ONLY valid JSON, no markdown or extra text):
            {
              "totalScore": <number>,
              "maxScore": <number>,
              "breakdown": [
                {
                  "criterion": "<rubric point name>",
                  "pointsEarned": <number>,
                  "pointsPossible": <number>,
                  "feedback": "<specific feedback>"
                }
              ],
              "overallFeedback": [
                "<actionable feedback point 1>",
                "<actionable feedback point 2>",
                "<actionable feedback point 3>"
              ],
              "strengths": ["<strength 1>", "<strength 2>"],
              "areasForImprovement": ["<improvement 1>", "<improvement 2>"]
            }
            """,
            frqData.getYear(),
            frqData.getQuestionNumber(),
            frqData.getTopic(),
            studentResponse,
            rubric
        );
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return extractTextFromGeminiResponse(response.getBody());
        } else {
            throw new RuntimeException("Gemini API returned status: " + response.getStatusCode());
        }
    }

    private String extractTextFromGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.get("candidates");
        
        if (candidates != null && candidates.isArray() && candidates.size() > 0) {
            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.get("content");
            JsonNode parts = content.get("parts");
            
            if (parts != null && parts.isArray() && parts.size() > 0) {
                JsonNode text = parts.get(0).get("text");
                return text.asText();
            }
        }
        
        throw new RuntimeException("Unable to extract text from Gemini response");
    }

    private FeedbackResponse parseAndValidate(String responseText) throws Exception {
        // Remove markdown code blocks if present
        String cleanText = responseText.trim()
            .replaceAll("```json\\n?", "")
            .replaceAll("```\\n?", "");

        FeedbackResponse feedback = objectMapper.readValue(cleanText, FeedbackResponse.class);

        // Validate required fields
        if (feedback.getTotalScore() == null || feedback.getMaxScore() == null ||
            feedback.getBreakdown() == null || feedback.getOverallFeedback() == null) {
            throw new IllegalArgumentException("Missing required fields in feedback response");
        }

        if (feedback.getBreakdown().isEmpty()) {
            throw new IllegalArgumentException("Breakdown cannot be empty");
        }

        return feedback;
    }

    // Inner classes for metadata
    public static class FRQMetadata {
        private String year;
        private Integer questionNumber;
        private String topic;

        // Constructors, getters, setters
        public FRQMetadata() {}

        public FRQMetadata(String year, Integer questionNumber, String topic) {
            this.year = year;
            this.questionNumber = questionNumber;
            this.topic = topic;
        }

        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
        public Integer getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
    }
}
