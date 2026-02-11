package com.open.spring.mvc.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/groups/chat")
@CrossOrigin
public class GroupChatApiController {

    private final GroupChatService groupChatService;
    private final GroupsJpaRepository groupsRepository;

    public GroupChatApiController(GroupChatService groupChatService, GroupsJpaRepository groupsRepository) {
        this.groupChatService = groupChatService;
        this.groupsRepository = groupsRepository;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadRequest {
        private String filename;
        private String base64Data;
    }

    // --- Auth helpers (commented out) ---
    // private String getCurrentUsername() {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     if (auth == null || !auth.isAuthenticated()) {
    //         return null;
    //     }
    //     String name = auth.getName();
    //     if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
    //         return null;
    //     }
    //     return name;
    // }
    //
    // private boolean isMember(Groups group, String uid) {
    //     return group.getGroupMembers().stream()
    //             .anyMatch(member -> uid.equals(member.getUid()));
    // }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long groupId) {
        Optional<Groups> groupOpt = groupsRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // String currentUsername = getCurrentUsername();
        // if (currentUsername == null) {
        //     return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // }
        // if (!isMember(groupOpt.get(), currentUsername)) {
        //     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        // }

        String groupName = groupOpt.get().getName();
        List<GroupChatMessage> messages = groupChatService.getMessages(groupName);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<?> postMessage(
            @PathVariable Long groupId,
            @RequestBody GroupChatMessage message) {
        Optional<Groups> groupOpt = groupsRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // String currentUsername = getCurrentUsername();
        // if (currentUsername == null) {
        //     return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // }
        // if (!isMember(groupOpt.get(), currentUsername)) {
        //     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        // }

        if (message == null || message.getName() == null || message.getMessage() == null) {
            return new ResponseEntity<>("name and message are required", HttpStatus.BAD_REQUEST);
        }

        String groupName = groupOpt.get().getName();
        List<GroupChatMessage> updated = groupChatService.addMessage(groupName, message);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @GetMapping("/{groupId}/files")
    public ResponseEntity<?> getSharedFiles(@PathVariable Long groupId) {
        Optional<Groups> groupOpt = groupsRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // String currentUsername = getCurrentUsername();
        // if (currentUsername == null) {
        //     return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // }
        // if (!isMember(groupOpt.get(), currentUsername)) {
        //     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        // }

        String groupName = groupOpt.get().getName();
        List<Map<String, String>> files = groupChatService.listSharedFiles(groupName);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @PostMapping("/{groupId}/files")
    public ResponseEntity<?> uploadSharedFile(
            @PathVariable Long groupId,
            @RequestBody FileUploadRequest request) {
        Optional<Groups> groupOpt = groupsRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // String currentUsername = getCurrentUsername();
        // if (currentUsername == null) {
        //     return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        // }
        // if (!isMember(groupOpt.get(), currentUsername)) {
        //     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        // }

        if (request == null || request.getFilename() == null || request.getBase64Data() == null) {
            return new ResponseEntity<>("filename and base64Data are required", HttpStatus.BAD_REQUEST);
        }

        String groupName = groupOpt.get().getName();
        String result = groupChatService.uploadSharedFile(groupName, request.getFilename(), request.getBase64Data());

        if (result != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("filename", request.getFilename());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
