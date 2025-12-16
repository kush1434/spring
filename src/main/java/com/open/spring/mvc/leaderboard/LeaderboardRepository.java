package com.open.spring.mvc.leaderboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, LeaderboardEntry.LeaderboardId> {
    
    // Find all entries ordered by score descending
    List<LeaderboardEntry> findAllByOrderByScoreDesc();
    
    // Find top N scores
    @Query("SELECT l FROM LeaderboardEntry l ORDER BY l.score DESC")
    List<LeaderboardEntry> findTopScores();
    
    // Find leaderboard entries for a specific user
    List<LeaderboardEntry> findByUserOrderByScoreDesc(String user);
    
    // Find leaderboard entries for a specific game
    List<LeaderboardEntry> findByGameNameOrderByScoreDesc(String gameName);
    
    // Find entries for a specific user and game
    List<LeaderboardEntry> findByUserAndGameNameOrderByScoreDesc(String user, String gameName);
    
    // Custom query to populate leaderboard from existing ScoreCounter table
    // This pulls user, game name, and score from your pause menu scores
    @Query(value = "SELECT sc.user as user_name, sc.game_name as game_name, sc.score " +
                   "FROM score_counter sc " +
                   "ORDER BY sc.score DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopScoresFromScoreTables(@Param("limit") int limit);
    
    // Get top scores for a specific game
    @Query(value = "SELECT sc.user as user_name, sc.game_name as game_name, sc.score " +
                   "FROM score_counter sc " +
                   "WHERE sc.game_name = :gameName " +
                   "ORDER BY sc.score DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopScoresForGame(@Param("gameName") String gameName, @Param("limit") int limit);
}