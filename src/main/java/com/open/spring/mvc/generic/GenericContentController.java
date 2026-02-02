package com.open.spring.mvc.generic;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.identity.User;
import com.open.spring.mvc.identity.UserRepository;

@RestController
@RequestMapping("/api/content")
public class GenericContentController {

    @Autowired
    private UserContentRepository contentRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{type}")
    public ResponseEntity<UserContent> createContent(
            @PathVariable ContentType type,
            @RequestBody UserContent content,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails != null) {
            String email = userDetails.getUsername();
            Optional<User> author = userRepository.findByEmail(email);

            if (author.isPresent()) {
                content.setAuthor(author.get());
            }
        }

        content.setType(type);
        UserContent saved = contentRepository.save(content);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{param}")
    public ResponseEntity<?> getContent(@PathVariable String param) {
        // Try to parse as Long (id)
        try {
            Long id = Long.parseLong(param);
            return contentRepository.findById(id)
                    .map(content -> new ResponseEntity<Object>(content, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (NumberFormatException e) {
            // Not a number, try as ContentType
            try {
                ContentType type = ContentType.valueOf(param.toUpperCase());
                return new ResponseEntity<>(contentRepository.findByType(type), HttpStatus.OK);
            } catch (IllegalArgumentException ex) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping("/{type}/{authorId}")
    public ResponseEntity<List<UserContent>> listContentByAuthor(@PathVariable ContentType type,
            @PathVariable Long authorId) {
        return new ResponseEntity<>(contentRepository.findByAuthorIdAndType(authorId, type), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserContent> updateContent(
            @PathVariable Long id,
            @RequestBody UserContent updatedContent,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<UserContent> existing = contentRepository.findById(id);
        if (existing.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserContent content = existing.get();
        String email = userDetails.getUsername();
        if (content.getAuthor() != null && !content.getAuthor().getEmail().equals(email)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Update fields
        content.setType(updatedContent.getType());
        content.setBody(updatedContent.getBody());
        content.setMetadata(updatedContent.getMetadata());

        UserContent saved = contentRepository.save(content);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<UserContent> content = contentRepository.findById(id);
        if (content.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Simple owner check
        // Note: Real world would need more robust permissions (admin vs owner)
        String email = userDetails.getUsername();
        if (content.get().getAuthor() != null && !content.get().getAuthor().getEmail().equals(email)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        contentRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
