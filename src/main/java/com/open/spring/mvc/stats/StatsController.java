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
     * GET /api/stats/all
     * Fulfills the request to see all columns (entries) in the DB.
     */
    @GetMapping("/all")
    public ResponseEntity<List<Stats>> getAllStats() {
        List<Stats> statsList = statsRepository.findAll();
        return new ResponseEntity<>(statsList, HttpStatus.OK);
    }

    /**
     * GET /api/stats/{username}
     * Helper endpoint to get stats for a single user.
     */
    @GetMapping("/{username}")
    public ResponseEntity<Stats> getStatsByUsername(@PathVariable String username) {
        Optional<Stats> optionalStats = statsRepository.findByUsername(username);
        if (!optionalStats.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(optionalStats.get(), HttpStatus.OK);
    }

    /**
     * POST /api/stats/create
     * A standard endpoint to create a new Stats record.
     * The request body should be a full Stats JSON object.
     */
    @PostMapping("/create")
    public ResponseEntity<Stats> createStats(@RequestBody Stats stats) {
        // Check if username already exists
        if (statsRepository.findByUsername(stats.getUsername()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Conflict, user exists
        }
        Stats newStats = statsRepository.save(stats);
        return new ResponseEntity<>(newStats, HttpStatus.CREATED);
    }

    /**
     * POST /api/stats/update/{username}
     * Fulfills the request to "add to a specific column for a specific username."
     * This endpoint updates a single field for an existing user.
     *
     * Request Body Example:
     * {
     * "column": "frontend",
     * "value": 95.5
     * }
     */
    @PostMapping("/update/{username}")
    public ResponseEntity<Stats> updateStats(
            @PathVariable String username,
            @RequestBody StatsUpdateDto updateRequest) {

        // 1. Find the user
        Optional<Stats> optionalStats = statsRepository.findByUsername(username);
        if (!optionalStats.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // User not found
        }

        Stats statsToUpdate = optionalStats.get();

        // 2. Use the DTO to figure out which column to update
        boolean updated = false;
        switch (updateRequest.getColumn().toLowerCase()) {
            case "frontend":
                statsToUpdate.setFrontend(updateRequest.getValue());
                updated = true;
                break;
            case "backend":
                statsToUpdate.setBackend(updateRequest.getValue());
                updated = true;
                break;
            case "data":
                statsToUpdate.setData(updateRequest.getValue());
                updated = true;
                break;
            case "resume":
                statsToUpdate.setResume(updateRequest.getValue());
                updated = true;
                break;
            case "ai":
                statsToUpdate.setAi(updateRequest.getValue());
                updated = true;
                break;
            default:
                // If the "column" name in the DTO doesn't match
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 3. Save the updated object
        Stats updatedStats = statsRepository.save(statsToUpdate);
        return new ResponseEntity<>(updatedStats, HttpStatus.OK);
    }
}