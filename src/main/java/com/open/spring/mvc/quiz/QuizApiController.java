package com.open.spring.mvc.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for handling quiz-related API endpoints.
 */
@RestController
@RequestMapping("/api/quiz")
@Validated
public class QuizApiController {

    @Autowired
    private QuizScoreRepository repository;

    /**
     * Save a user's quiz score.
     *
     * @param request the incoming score request payload
     * @return the saved QuizScore entity with HTTP 201 CREATED
     */
    @PostMapping("/score")
    public ResponseEntity<QuizScore> saveScore(@Valid @RequestBody QuizScoreRequest request) {
        QuizScore entity = new QuizScore();
        entity.setUsername(request.getUsername());
        entity.setScore(request.getScore());

        QuizScore saved = repository.save(entity);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Return the top quiz scores, optionally limited via a query param (?limit=10).
     *
     * @param limit optional maximum number of scores to return
     * @return list of top scores sorted descending by score
     */
    @GetMapping("/top")
    public ResponseEntity<List<QuizScore>> topScores(@RequestParam(name = "limit", required = false) Integer limit) {
        List<QuizScore> all = repository.findAllOrderByScoreDesc();

        // Apply limit if specified
        List<QuizScore> out = all;
        if (limit != null && limit > 0) {
            out = all.stream().limit(limit).collect(Collectors.toList());
        }

        return new ResponseEntity<>(out, HttpStatus.OK);
    }

    /**
     * Return all quiz scores for a specific user, sorted descending by score.
     *
     * @param username the username to look up
     * @return list of scores for the given user
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<QuizScore>> scoresForUser(@PathVariable String username) {
        List<QuizScore> scores = repository.findByUsernameIgnoreCaseOrderByScoreDesc(username);
        return new ResponseEntity<>(scores, HttpStatus.OK);
    }
}