package com.nighthawk.spring_portfolio.mvc.forum.forumComments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nighthawk.spring_portfolio.mvc.forum.ForumRepository;
import java.util.Optional;

import com.nighthawk.spring_portfolio.mvc.forum.Forum;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class forumCommentsController {

    @Autowired
    private forumCommentsRepository repository;

    @Autowired
    private ForumRepository forumRepository;

    // Create a new comment with dynamic author logic
    @PostMapping("/post")
    public ResponseEntity<String> createComment(@RequestBody RequestBodyData requestBodyData) {
        // Ensure comment content and forumId are provided
        if (requestBodyData.getComment() == null || requestBodyData.getComment().isEmpty() ||
            requestBodyData.getForumId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Comment or Forum ID is required.");
        }

        // If no author is provided, assign a random anonymous author
        if (requestBodyData.getAuthor() == null || requestBodyData.getAuthor().isEmpty()) {
            String[] authorEnding = {"Whale", "Pig", "Badger", "Warthog", "Fish", "Cow", "Chicken", "Rabbit", "Wolf", "Bear"};
            requestBodyData.setAuthor("Anonymous" + authorEnding[(int) (Math.random() * 10)]);
        }

        // Fetch the related Forum entity based on forumId
        Optional<Forum> forumOptional = forumRepository.findById(requestBodyData.getForumId());
        if (!forumOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Forum not found.");
        }

        // Get the Forum entity
        Forum forum = forumOptional.get();

        String comment = requestBodyData.getComment();
        String author = requestBodyData.getAuthor();
        String timestamp = java.time.LocalDate.now().toString();

        // Create and save the new comment
        forumComments newComment = new forumComments(author, comment, forum);
        repository.save(newComment); // Save to repository

        return ResponseEntity.status(HttpStatus.CREATED).body("Comment successfully added to the forum post.");
    }

    // Get all comments for a specific forum
    @GetMapping("/get/{forumId}")
    public ResponseEntity<List<forumComments>> getCommentsByForum(@PathVariable Long forumId) {
        List<forumComments> comments = repository.findByForumId(forumId);
        System.out.println(comments);
        return ResponseEntity.ok(comments);
    }

    // Get all comments (optional)
    @GetMapping("/all")
    public ResponseEntity<List<forumComments>> getAllComments() {
        List<forumComments> comments = repository.findAll();

    // Debugging output
    if (comments.isEmpty()) {
        System.out.println("No comments found in database.");
    } else {
        System.out.println("Comments retrieved: " + comments);
    }

    return ResponseEntity.ok(comments);
    }

    // Update an existing comment
    @PutMapping("/update/{id}")
    public ResponseEntity<forumComments> updateComment(@PathVariable Long id, 
                                                       @RequestBody forumComments updatedComment, 
                                                       @RequestParam String username) {
        forumComments comment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        
        System.out.println(username);

        // Ensure the user updating is the original author
        if (!comment.getAuthor().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Return 403 Forbidden
        }

        

        // Only allow updating the comment content, not author or forum
        comment.setComment(updatedComment.getComment());
        comment.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(repository.save(comment));
    }

    // Delete a comment
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @RequestParam String username) {
        forumComments comment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        repository.delete(comment);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    public static class RequestBodyData {
        private String comment;
        private String author;
        private Long forumId; // Reference to the forum post
        private String timestamp;

        // Getters and Setters
        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Long getForumId() {
            return forumId;
        }

        public void setForumId(Long forumId) {
            this.forumId = forumId;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}