package com.open.spring.mvc.person;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// ... existing imports and class ...

@Service
public class PersonService {

    @Autowired
    private PersonJpaRepository repository;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    private final Gson gson = new Gson();

    public void gradeAllPending() {
        List<Person> persons = repository.findAll();

        for (Person p : persons) {
            List<Map<String, Object>> grades = p.getGradesJson();
            if (grades == null || grades.isEmpty()) continue;

            try {
                JsonArray entries = gson.toJsonTree(grades).getAsJsonArray();
                for (JsonElement e : entries) {
                    JsonObject obj = e.getAsJsonObject();

                    String questionUrl = obj.has("assignment") ? obj.get("assignment").getAsString() : "";
                    String submissionUrl = obj.has("submission") ? obj.get("submission").getAsString() : "";

                    if (!questionUrl.startsWith("http") || !submissionUrl.startsWith("http")) continue;

                    String geminiResponse = callGemini(questionUrl, submissionUrl);
                    appendToCsv(p, obj.has("assignment") ? obj.get("assignment").getAsString() : "", geminiResponse);
                }
            } catch (Exception ex) {
                System.err.println("Failed to process person " + p.getUid() + ": " + ex.getMessage());
            }
        }
    }

    private String callGemini(String questionUrl, String submissionUrl) {
        try {
            Client client = Client.builder().apiKey(geminiApiKey).build();

            String prompt = String.format("""
                Fetch the full FRQ question text, description, requirements, examples and rubric from:
                %s

                Fetch the student's submitted code from:
                %s

                Extract ONLY the Java code / class implementation from the student page.
                Ignore navigation, reflections, headers, footers, images, unrelated text.

                Grade the extracted code strictly 0–9 according to the question requirements.
                Award partial credit where appropriate.

                Output ONLY this exact format (nothing else):
                Score: X/9
                Comments: [detailed feedback, point-by-point, bugs, suggestions...]
                """, questionUrl, submissionUrl);

            GenerateContentResponse resp = client.models.generateContent("models/gemini-2.5-flash", prompt, null);
            return resp.text().trim();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void appendToCsv(Person p, String assignment, String geminiResponse) {
        String path = "./volumes/grades.csv";
        String name = p.getName() != null ? p.getName() : "";
        String uid = p.getUid();

        String escaped = geminiResponse
                .replace("\"", "\"\"")
                .replace("\n", " | ");

        String row = String.format("%s,%s,%s,\"%s\"\n", name, uid, assignment, escaped);

        try {
            java.nio.file.Path csvPath = Paths.get(path);
            if (!Files.exists(csvPath)) {
                Files.writeString(csvPath, "Name,UID,Assignment,Response\n");
            }
            Files.writeString(csvPath, row, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("CSV append failed: " + e.getMessage());
        }
    }

    // ... keep any existing methods ...
}