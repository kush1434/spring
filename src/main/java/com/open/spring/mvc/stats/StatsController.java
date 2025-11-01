package com.open.spring.mvc.stats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsRepository statsRepository;

    /**
     * GET /api/stats
     * Get stats for a specific user or all users.
     * 
     * Request Body Examples:
     * To get a specific user: {"username": "toby"}
     * To get all users: {} or {"username": null}
     */
    @GetMapping
    public ResponseEntity<?> getStats(@RequestBody(required = false) StatsGetDto getRequest) {
        // If no body or username is null/empty, return all stats
        if (getRequest == null || getRequest.getUsername() == null || getRequest.getUsername().isEmpty()) {
            List<Stats> statsList = statsRepository.findAll();
            return new ResponseEntity<>(statsList, HttpStatus.OK);
        }
        
        // Otherwise, get stats for the specific username
        Optional<Stats> optionalStats = statsRepository.findByUsername(getRequest.getUsername());
        if (!optionalStats.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(optionalStats.get(), HttpStatus.OK);
    }

    /**
     * POST /api/stats
     * Create a new Stats record.
     * 
     * Request Body Example:
     * {
     * "username": "toby",
     * "frontend": 20.0,
     * "backend": 30.0,
     * "data": 40.0,
     * "resume": 50.0,
     * "ai": 60.0
     * }
     */
    @PostMapping
    public ResponseEntity<Stats> createStats(@RequestBody Stats stats) {
        if (statsRepository.findByUsername(stats.getUsername()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Conflict, user exists
        }
        Stats newStats = statsRepository.save(stats);
        return new ResponseEntity<>(newStats, HttpStatus.CREATED);
    }

    /**
     * PUT /api/stats
     * Update stats for a specific column.
     * 
     * Request Body Example:
     * {
     * "username": "toby",
     * "column": "frontend",
     * "value": 95.5
     * }
     */
    @PutMapping
    public ResponseEntity<Stats> updateStats(@RequestBody StatsUpdateDto updateRequest) {

        // 1. Find the user from the DTO
        Optional<Stats> optionalStats = statsRepository.findByUsername(updateRequest.getUsername());
        if (!optionalStats.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // User not found
        }

        Stats statsToUpdate = optionalStats.get();

        // 2. Use the DTO to figure out which column to update
        switch (updateRequest.getColumn().toLowerCase()) {
            case "frontend":
                statsToUpdate.setFrontend(updateRequest.getValue());
                break;
            case "backend":
                statsToUpdate.setBackend(updateRequest.getValue());
                break;
            case "data":
                statsToUpdate.setData(updateRequest.getValue());
                break;
            case "resume":
                statsToUpdate.setResume(updateRequest.getValue());
                break;
            case "ai":
                statsToUpdate.setAi(updateRequest.getValue());
                break;
            default:
                // If the "column" name in the DTO doesn't match
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 3. Save the updated object
        Stats updatedStats = statsRepository.save(statsToUpdate);
        return new ResponseEntity<>(updatedStats, HttpStatus.OK);
    }

    /**
     * DELETE /api/stats
     * Delete a user's stats.
     * 
     * Request Body Example:
     * {
     * "username": "toby"
     * }
     */
    @DeleteMapping
    public ResponseEntity<String> deleteStats(@RequestBody StatsDeleteDto deleteRequest) {

        // 1. Find the user by username from the DTO
        Optional<Stats> optionalStats = statsRepository.findByUsername(deleteRequest.getUsername());
        if (!optionalStats.isPresent()) {
            // Return Not Found if the user doesn't exist
            return new ResponseEntity<>("User '" + deleteRequest.getUsername() + "' not found.", HttpStatus.NOT_FOUND);
        }

        // 2. Get the stats object and delete it
        Stats statsToDelete = optionalStats.get();
        statsRepository.delete(statsToDelete);

        // 3. Return a success message
        return new ResponseEntity<>("Stats for '" + deleteRequest.getUsername() + "' deleted successfully.", HttpStatus.OK);
    }
}