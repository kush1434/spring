package com.open.spring.mvc.data_viz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.open.spring.mvc.userdata.UserData;
import com.open.spring.mvc.userdata.UserDataJpaRepository;

import java.time.Instant;
import java.util.*;

/**
 * REST Controller for storing and managing user-related data such as lesson progress,
 * uploaded files, and Gemini grades. All requests begin with /api/userdata.
 */
@RestController // annotation simplifies the creation of RESTful web services
@RequestMapping("/api/userdata") // all requests in this file begin with this URI
public class DataVizApiController {

    // Autowired enables Spring to inject the repository for easy database CRUD operations
    @Autowired
    private UserDataJpaRepository repository;

    // ObjectMapper used for converting between JSON strings and objects
    private final ObjectMapper mapper = new ObjectMapper();

    /* GET all user data
     * @GetMapping maps HTTP GET requests onto this handler method.
     */
    @GetMapping("/")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            for (UserData data : repository.findAll()) {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", data.getUserId());
                item.put("data", mapper.readTree(data.getDataJson())); // parse JSON text back to an object
                result.add(item);
            }
            // ResponseEntity returns List of all user data with status code 200
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* GET user data by ID
     * @PathVariable extracts the {userId} part from the URI
     */
    @GetMapping("/{userId}")
    public ResponseEntity<JsonNode> getUserData(@PathVariable String userId) {
        try {
            Optional<UserData> optional = repository.findByUserId(userId);
            if (optional.isPresent()) {
                // Return stored JSON blob as JSON
                return new ResponseEntity<>(mapper.readTree(optional.get().getDataJson()), HttpStatus.OK);
            }
            // Bad ID
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* POST new or update existing user JSON blob
     * @RequestBody takes JSON from the HTTP body
     */
    @PostMapping("/{userId}")
    public ResponseEntity<JsonNode> upsertUserData(@PathVariable String userId, @RequestBody JsonNode body) {
        try {
            ObjectNode ensured = ensureUserId(body, userId);
            String json = mapper.writeValueAsString(ensured);

            // If user exists, update their record; otherwise create new
            UserData record = repository.findByUserId(userId).orElse(new UserData(userId, json));
            record.setDataJson(json);
            repository.save(record);

            // OK HTTP response with stored JSON blob
            return new ResponseEntity<>(ensured, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /* PATCH merge JSON fields
     * Merges new data into existing blob rather than replacing it
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<JsonNode> mergeUserData(@PathVariable String userId, @RequestBody ObjectNode patch) {
        try {
            ObjectNode existing = loadOrInit(userId);
            existing.setAll(patch); // shallow merge
            existing.put("userId", userId);
            return saveAndReturn(userId, existing);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /* POST append lesson progress
     * Adds a lesson progress record to the JSON array
     */
    @PostMapping("/{userId}/progress")
    public ResponseEntity<JsonNode> addLessonProgress(@PathVariable String userId, @RequestBody ObjectNode progress) {
        try {
            ObjectNode root = loadOrInit(userId);
            var arr = root.withArray("lessonProgress");
            if (!progress.has("lastViewed")) progress.put("lastViewed", Instant.now().toString());
            arr.add(progress);
            return saveAndReturn(userId, root);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /* POST append uploaded file metadata
     * Records metadata (not file content) into uploadedFiles[]
     */
    @PostMapping("/{userId}/files")
    public ResponseEntity<JsonNode> addFileMeta(@PathVariable String userId, @RequestBody ObjectNode fileMeta) {
        try {
            ObjectNode root = loadOrInit(userId);
            var arr = root.withArray("uploadedFiles");
            if (!fileMeta.has("uploadedAt")) fileMeta.put("uploadedAt", Instant.now().toString());
            arr.add(fileMeta);
            return saveAndReturn(userId, root);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /* POST append Gemini grade entry
     * Adds a Gemini grade record to geminiGrades[]
     */
    @PostMapping("/{userId}/grades")
    public ResponseEntity<JsonNode> addGeminiGrade(@PathVariable String userId, @RequestBody ObjectNode grade) {
        try {
            ObjectNode root = loadOrInit(userId);
            var arr = root.withArray("geminiGrades");
            arr.add(grade);
            return saveAndReturn(userId, root);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // -------- Helper Methods --------

    // Ensures a new user JSON object always includes required arrays
    private ObjectNode ensureUserId(JsonNode body, String userId) {
        ObjectNode node = body.isObject() ? (ObjectNode) body : mapper.createObjectNode();
        node.put("userId", userId);
        if (!node.has("lessonProgress")) node.putArray("lessonProgress");
        if (!node.has("uploadedFiles")) node.putArray("uploadedFiles");
        if (!node.has("geminiGrades")) node.putArray("geminiGrades");
        return node;
    }

    // Loads an existing user record or initializes a new one
    private ObjectNode loadOrInit(String userId) {
        return (ObjectNode) repository.findByUserId(userId)
                .map(row -> safeRead(row.getDataJson()))
                .orElseGet(() -> ensureUserId(mapper.createObjectNode(), userId));
    }

    // Safely parses JSON or returns empty ObjectNode
    private ObjectNode safeRead(String json) {
        try {
            JsonNode n = mapper.readTree(json);
            return n.isObject() ? (ObjectNode) n : mapper.createObjectNode();
        } catch (Exception e) {
            return mapper.createObjectNode();
        }
    }

    // Persists JSON blob and returns updated content
    private ResponseEntity<JsonNode> saveAndReturn(String userId, ObjectNode root) {
        try {
            String json = mapper.writeValueAsString(root);
            UserData record = repository.findByUserId(userId).orElse(new UserData(userId, json));
            record.setDataJson(json);
            repository.save(record);
            return new ResponseEntity<>(root, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
