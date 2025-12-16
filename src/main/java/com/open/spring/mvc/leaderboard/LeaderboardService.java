package com.open.spring.mvc.leaderboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {
    
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    /**
     * Get top N entries from the leaderboard
     */
    public List<LeaderboardEntry> getTopScores(int limit) {
        return leaderboardRepository.findTopScores()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all leaderboard entries ordered by score
     */
    public List<LeaderboardEntry> getAllEntriesByScore() {
        return leaderboardRepository.findAllByOrderByScoreDesc();
    }
    
    /**
     * Get leaderboard entries for a specific game
     */
    public List<LeaderboardEntry> getEntriesByGame(String gameName) {
        return leaderboardRepository.findByGameNameOrderByScoreDesc(gameName);
    }
    
    /**
     * Refresh leaderboard from the scores table
     * This pulls from your ScoreCounter table (pause menu scores)
     */
    @Transactional
    public void refreshLeaderboard(int topN) {
        // Clear existing leaderboard
        leaderboardRepository.deleteAll();
        
        // Get top scores from ScoreCounter table
        List<Object[]> topScores = leaderboardRepository.getTopScoresFromScoreTables(topN);
        
        // Convert to LeaderboardEntry objects and save
        List<LeaderboardEntry> entries = topScores.stream()
                .map(row -> new LeaderboardEntry(
                        (String) row[0],  // user (from ScoreCounter)
                        (String) row[1],  // game_name (from ScoreCounter)
                        (Integer) row[2]  // score (from ScoreCounter)
                ))
                .collect(Collectors.toList());
        
        leaderboardRepository.saveAll(entries);
    }
    
    /**
     * Refresh leaderboard for a specific game
     */
    @Transactional
    public void refreshLeaderboardForGame(String gameName, int topN) {
        // Delete existing entries for this game
        List<LeaderboardEntry> existingEntries = leaderboardRepository.findByGameNameOrderByScoreDesc(gameName);
        leaderboardRepository.deleteAll(existingEntries);
        
        // Get top scores for this game from ScoreCounter
        List<Object[]> topScores = leaderboardRepository.getTopScoresForGame(gameName, topN);
        
        // Convert to LeaderboardEntry objects and save
        List<LeaderboardEntry> entries = topScores.stream()
                .map(row -> new LeaderboardEntry(
                        (String) row[0],  // user (from ScoreCounter)
                        (String) row[1],  // game_name (from ScoreCounter)
                        (Integer) row[2]  // score (from ScoreCounter)
                ))
                .collect(Collectors.toList());
        
        leaderboardRepository.saveAll(entries);
    }
    
    /**
     * Add a single entry to the leaderboard
     */
    public LeaderboardEntry addEntry(String user, String gameName, Integer score) {
        LeaderboardEntry entry = new LeaderboardEntry(user, gameName, score);
        return leaderboardRepository.save(entry);
    }
    
    /**
     * Get leaderboard entries for a specific user
     */
    public List<LeaderboardEntry> getUserEntries(String user) {
        return leaderboardRepository.findByUserOrderByScoreDesc(user);
    }
    
    /**
     * Get entries for a specific user and game
     */
    public List<LeaderboardEntry> getUserGameEntries(String user, String gameName) {
        return leaderboardRepository.findByUserAndGameNameOrderByScoreDesc(user, gameName);
    }
    
    /**
     * Get all leaderboard entries
     */
    public List<LeaderboardEntry> getAllEntries() {
        return leaderboardRepository.findAll();
    }
    
    /**
     * Get a single leaderboard entry by user and game
     */
    public LeaderboardEntry getEntryByUserAndGame(String user, String gameName) {
        LeaderboardEntry.LeaderboardId id = new LeaderboardEntry.LeaderboardId(user, gameName);
        return leaderboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leaderboard entry not found for user: " + user + ", game: " + gameName));
    }
    
    /**
     * Update an existing leaderboard entry
     */
    @Transactional
    public LeaderboardEntry updateEntry(String user, String gameName, Integer score) {
        LeaderboardEntry entry = getEntryByUserAndGame(user, gameName);
        
        if (score != null) {
            entry.setScore(score);
        }
        
        return leaderboardRepository.save(entry);
    }
    
    /**
     * Delete a leaderboard entry by user and game
     */
    @Transactional
    public void deleteEntry(String user, String gameName) {
        LeaderboardEntry.LeaderboardId id = new LeaderboardEntry.LeaderboardId(user, gameName);
        if (!leaderboardRepository.existsById(id)) {
            throw new RuntimeException("Leaderboard entry not found for user: " + user + ", game: " + gameName);
        }
        leaderboardRepository.deleteById(id);
    }
    
    /**
     * Delete all leaderboard entries
     */
    @Transactional
    public void deleteAllEntries() {
        leaderboardRepository.deleteAll();
    }
}