package com.open.spring.mvc.leaderboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.Data;
import java.util.List;

/**
 * API Controller for Leaderboard Management
 */
@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    /**
     * CREATE - Add a new leaderboard entry
     * POST /api/leaderboard
     */
    @PostMapping
    public ResponseEntity<LeaderboardEntry> createEntry(@RequestBody LeaderboardEntryRequest request) {
        LeaderboardEntry entry = leaderboardService.addEntry(
                request.getUser(), 
                request.getGameName(),
                request.getScore()
        );
        return new ResponseEntity<>(entry, HttpStatus.CREATED);
    }
    
    /**
     * READ - Get all leaderboard entries ordered by score
     * GET /api/leaderboard
     */
    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> getAllEntries() {
        List<LeaderboardEntry> entries = leaderboardService.getAllEntriesByScore();
        return ResponseEntity.ok(entries);
    }
    
    /**
     * READ - Get a single leaderboard entry by user and game
     * GET /api/leaderboard/{user}/{gameName}
     */
    @GetMapping("/{user}/{gameName}")
    public ResponseEntity<LeaderboardEntry> getEntryByUserAndGame(
            @PathVariable String user, 
            @PathVariable String gameName) {
        LeaderboardEntry entry = leaderboardService.getEntryByUserAndGame(user, gameName);
        return ResponseEntity.ok(entry);
    }
    
    /**
     * READ - Get top N scores
     * GET /api/leaderboard/top/{limit}
     */
    @GetMapping("/top/{limit}")
    public ResponseEntity<List<LeaderboardEntry>> getTopScores(@PathVariable int limit) {
        List<LeaderboardEntry> entries = leaderboardService.getTopScores(limit);
        return ResponseEntity.ok(entries);
    }
    
    /**
     * READ - Get leaderboard entries for a specific game
     * GET /api/leaderboard/game/{gameName}
     */
    @GetMapping("/game/{gameName}")
    public ResponseEntity<List<LeaderboardEntry>> getEntriesByGame(@PathVariable String gameName) {
        List<LeaderboardEntry> entries = leaderboardService.getEntriesByGame(gameName);
        return ResponseEntity.ok(entries);
    }
    
    /**
     * READ - Get leaderboard entries for a specific user
     * GET /api/leaderboard/user/{user}
     */
    @GetMapping("/user/{user}")
    public ResponseEntity<List<LeaderboardEntry>> getUserEntries(@PathVariable String user) {
        List<LeaderboardEntry> entries = leaderboardService.getUserEntries(user);
        return ResponseEntity.ok(entries);
    }
    
    /**
     * READ - Get entries for a specific user and game
     * GET /api/leaderboard/user/{user}/game/{gameName}
     */
    @GetMapping("/user/{user}/game/{gameName}")
    public ResponseEntity<List<LeaderboardEntry>> getUserGameEntries(
            @PathVariable String user, 
            @PathVariable String gameName) {
        List<LeaderboardEntry> entries = leaderboardService.getUserGameEntries(user, gameName);
        return ResponseEntity.ok(entries);
    }
    
    /**
     * UPDATE - Update an existing leaderboard entry
     * PUT /api/leaderboard/{user}/{gameName}
     */
    @PutMapping("/{user}/{gameName}")
    public ResponseEntity<LeaderboardEntry> updateEntry(
            @PathVariable String user,
            @PathVariable String gameName,
            @RequestBody LeaderboardEntryRequest request) {
        LeaderboardEntry updated = leaderboardService.updateEntry(
                user, 
                gameName,
                request.getScore()
        );
        return ResponseEntity.ok(updated);
    }
    
    /**
     * DELETE - Delete a leaderboard entry by user and game
     * DELETE /api/leaderboard/{user}/{gameName}
     */
    @DeleteMapping("/{user}/{gameName}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable String user,
            @PathVariable String gameName) {
        leaderboardService.deleteEntry(user, gameName);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DELETE - Delete all leaderboard entries
     * DELETE /api/leaderboard
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllEntries() {
        leaderboardService.deleteAllEntries();
        return ResponseEntity.noContent().build();
    }
    
    /**
     * REFRESH - Refresh entire leaderboard from score tables
     * POST /api/leaderboard/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshLeaderboard(@RequestParam(defaultValue = "100") int topN) {
        leaderboardService.refreshLeaderboard(topN);
        return ResponseEntity.ok("Leaderboard refreshed with top " + topN + " scores");
    }
    
    /**
     * REFRESH - Refresh leaderboard for a specific game
     * POST /api/leaderboard/refresh/game/{gameName}
     */
    @PostMapping("/refresh/game/{gameName}")
    public ResponseEntity<String> refreshLeaderboardForGame(
            @PathVariable String gameName,
            @RequestParam(defaultValue = "100") int topN) {
        leaderboardService.refreshLeaderboardForGame(gameName, topN);
        return ResponseEntity.ok("Leaderboard refreshed for game '" + gameName + "' with top " + topN + " scores");
    }
    
    // DTO for request body
    @Data
    public static class LeaderboardEntryRequest {
        private String user;
        private String gameName;
        private Integer score;
    }
}