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
     * {
     * "column": "frontend",
     * "value": 95.5
     * }
     * 
     * This currently doesn't work. needs to fix!!
     */
    @PostMapping("/update/{username}")
    public ResponseEntity<Stats> updateStats(
            @PathVariable String username,
            @RequestBody StatsUpdateDto updateRequest) {

        // 1. Find the user
        Optional<Stats> optionalStats = statsRepository.findByUsername(username);
        if (!optionalStats.isPresent()) {
            return ResponseEntity.notFound().build(); // User not found
        }

        Stats statsToUpdate = optionalStats.get();

        // 2. Use the DTO to figure out which column to update
        switch (updateRequest.getColumn().toLowerCase()) {
            case "frontend" -> statsToUpdate.setFrontend(updateRequest.getValue());
            case "backend" -> statsToUpdate.setBackend(updateRequest.getValue());
            case "data" -> statsToUpdate.setData(updateRequest.getValue());
            case "resume" -> statsToUpdate.setResume(updateRequest.getValue());
            case "ai" -> statsToUpdate.setAi(updateRequest.getValue());
            default -> return ResponseEntity.badRequest().build();
        }

        // 3. Save the updated object
        Stats updatedStats = statsRepository.save(statsToUpdate);
        return ResponseEntity.ok(updatedStats);
    }
}