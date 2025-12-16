package com.open.spring.mvc.leaderboard;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "leaderboard")
@IdClass(LeaderboardEntry.LeaderboardId.class)
public class LeaderboardEntry {
    
    @Id
    @Column(name = "user", nullable = false)
    private String user;
    
    @Id
    @Column(name = "game_name", nullable = false)
    private String gameName;
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    // Constructors
    public LeaderboardEntry() {}
    
    public LeaderboardEntry(String user, String gameName, Integer score) {
        this.user = user;
        this.gameName = gameName;
        this.score = score;
    }
    
    // Getters and Setters
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getGameName() {
        return gameName;
    }
    
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    // Composite Primary Key Class
    public static class LeaderboardId implements Serializable {
        private String user;
        private String gameName;
        
        public LeaderboardId() {}
        
        public LeaderboardId(String user, String gameName) {
            this.user = user;
            this.gameName = gameName;
        }
        
        // Getters and Setters
        public String getUser() {
            return user;
        }
        
        public void setUser(String user) {
            this.user = user;
        }
        
        public String getGameName() {
            return gameName;
        }
        
        public void setGameName(String gameName) {
            this.gameName = gameName;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LeaderboardId that = (LeaderboardId) o;
            return Objects.equals(user, that.user) && 
                   Objects.equals(gameName, that.gameName);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(user, gameName);
        }
    }
}