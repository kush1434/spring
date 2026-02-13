package com.open.spring.mvc.groups;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.spring.mvc.S3uploads.S3FileHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatService {
    private static final String MESSAGES_FILE = "messages-images/messages.jsonl";
    private static final String SHARED_FILES_PREFIX = "shared-files/";

    private final S3FileHandler s3FileHandler;
    private final ObjectMapper objectMapper;

    public void initGroupStorage(String groupName) {
        // Create empty messages.jsonl file
        String emptyBase64 = Base64.getEncoder().encodeToString(new byte[0]);
        s3FileHandler.uploadFile(emptyBase64, MESSAGES_FILE, groupName);

        // Create shared-files/ folder (S3 uses a zero-byte placeholder to represent an empty folder)
        s3FileHandler.uploadFile(emptyBase64, SHARED_FILES_PREFIX, groupName);
    }

    public List<GroupChatMessage> getMessages(String groupName) {
        String base64Data = s3FileHandler.decodeFile(groupName, MESSAGES_FILE);
        if (base64Data == null || base64Data.isBlank()) {
            return new ArrayList<>();
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid base64 data for group {} messages.", groupName, e);
            return new ArrayList<>();
        }

        String jsonl = new String(decoded, StandardCharsets.UTF_8);
        if (jsonl.isBlank()) {
            return new ArrayList<>();
        }

        List<GroupChatMessage> messages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(jsonl))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    GroupChatMessage msg = objectMapper.readValue(line, GroupChatMessage.class);
                    messages.add(msg);
                } catch (Exception e) {
                    log.warn("Skipping invalid message line for group {}: {}", groupName, line, e);
                }
            }
        } catch (Exception e) {
            log.warn("Failed reading messages for group {}", groupName, e);
        }

        return messages;
    }

    public List<GroupChatMessage> addMessage(String groupName, GroupChatMessage message) {
        List<GroupChatMessage> messages = getMessages(groupName);
        messages.add(message);

        String jsonl = messages.stream()
                .map(this::toJson)
                .collect(Collectors.joining("\n"));

        String base64Data = Base64.getEncoder()
                .encodeToString(jsonl.getBytes(StandardCharsets.UTF_8));

        s3FileHandler.uploadFile(base64Data, MESSAGES_FILE, groupName);
        return messages;
    }

    public List<Map<String, String>> listSharedFiles(String groupName) {
        String prefix = groupName + "/" + SHARED_FILES_PREFIX;
        List<String> keys = s3FileHandler.listFiles(prefix);

        List<String> filenames = keys.stream()
                .map(key -> key.substring(prefix.length()))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        List<Map<String, String>> files = new ArrayList<>();
        for (String filename : filenames) {
            Map<String, String> fileEntry = new HashMap<>();
            fileEntry.put("filename", filename);
            String base64Data = s3FileHandler.decodeFile(groupName, SHARED_FILES_PREFIX + filename);
            fileEntry.put("base64Data", base64Data);
            files.add(fileEntry);
        }
        return files;
    }

    public String uploadSharedFile(String groupName, String filename, String base64Data) {
        return s3FileHandler.uploadFile(base64Data, SHARED_FILES_PREFIX + filename, groupName);
    }

    public String downloadSharedFile(String groupName, String filename) {
        return s3FileHandler.decodeFile(groupName, SHARED_FILES_PREFIX + filename);
    }

    private String toJson(GroupChatMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.warn("Failed to serialize message: {}", message, e);
            return "{}";
        }
    }
}
