package com.open.spring.mvc.assignments;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.open.spring.mvc.S3uploads.FileHandler;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

@RestController
@RequestMapping("/api/assignment-submissions")
public class AssignmentSubmissionFileApiController {

    @Autowired
    private FileHandler fileHandler;

    @Autowired
    private PersonJpaRepository personRepo;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadAssignmentSubmission(
            @RequestParam("assignmentName") String assignmentName,
            @RequestParam("userId") Long userId,
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return new ResponseEntity<>(error("Authentication required"), HttpStatus.UNAUTHORIZED);
        }

        if (isBlank(assignmentName) || isBlank(username) || userId == null) {
            return new ResponseEntity<>(error("assignmentName, userId, and username are required"), HttpStatus.BAD_REQUEST);
        }

        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>(error("file is required"), HttpStatus.BAD_REQUEST);
        }

        Person authenticatedUser = personRepo.findByUid(userDetails.getUsername());
        if (authenticatedUser == null) {
            return new ResponseEntity<>(error("Authenticated user not found"), HttpStatus.UNAUTHORIZED);
        }

        Person targetUser = personRepo.findById(userId).orElse(null);
        if (targetUser == null) {
            return new ResponseEntity<>(error("Target user not found for userId=" + userId), HttpStatus.NOT_FOUND);
        }

        if (!username.equalsIgnoreCase(targetUser.getUid()) && !username.equalsIgnoreCase(targetUser.getName())) {
            return new ResponseEntity<>(error("username does not match the provided userId"), HttpStatus.BAD_REQUEST);
        }

        boolean privileged = authenticatedUser.hasRoleWithName("ROLE_TEACHER") || authenticatedUser.hasRoleWithName("ROLE_ADMIN");
        if (!privileged && !Objects.equals(authenticatedUser.getId(), userId)) {
            return new ResponseEntity<>(error("You can only submit assignments for your own account"), HttpStatus.FORBIDDEN);
        }

        try {
            String originalFilename = Paths.get(file.getOriginalFilename() == null ? "submission.bin" : file.getOriginalFilename())
                    .getFileName()
                    .toString();
            String assignmentSlug = slugify(assignmentName);
            String s3Filename = "assignment-submissions/"
                    + assignmentSlug
                    + "/"
                    + Instant.now().toEpochMilli()
                    + "_"
                    + UUID.randomUUID()
                    + "_"
                    + originalFilename;

            String base64Data = Base64.getEncoder().encodeToString(file.getBytes());
            String result = fileHandler.uploadFile(base64Data, s3Filename, targetUser.getUid());

            if (result == null) {
                return new ResponseEntity<>(error("Upload failed. Check S3 configuration and retry."), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Assignment submission uploaded successfully");
            response.put("assignmentName", assignmentName);
            response.put("userId", targetUser.getId());
            response.put("username", targetUser.getUid());
            response.put("displayName", targetUser.getName());
            response.put("storedFilename", result);
            response.put("storagePath", targetUser.getUid() + "/" + s3Filename);
            response.put("originalFilename", originalFilename);
            response.put("contentType", file.getContentType());
            response.put("size", file.getSize());
            response.put("notes", notes);
            response.put("uploadedBy", authenticatedUser.getUid());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(error("Error uploading submission: " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, String> error(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    private String slugify(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).trim();
        String slug = normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "assignment" : slug;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}