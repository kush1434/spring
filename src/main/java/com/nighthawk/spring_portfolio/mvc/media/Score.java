package com.nighthawk.spring_portfolio.mvc.media;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String personName;

    private int score;

    public String getPersonName() {
        return this.personName;
    }
    
    public int getScore() {
        return this.score;
    }

    public Score(String personName, int score) {
        this.personName = personName;
        this.score = score;
    }

    @Service
    public static class ScoreService {
        
        @Autowired
        private MediaJpaRepository mediaJpaRepository;

        @PostConstruct
        public void initializeScores() {
            if (mediaJpaRepository == null) {
                throw new RuntimeException("Leaderboard is not initialized!");
            }
            
            List<Score> scores = new ArrayList<>();
            scores.add(new Score("Thomas Edison", 0));

            for (Score score : scores) {
                List<Score> existingPlayers = mediaJpaRepository.findByPersonName(score.personName);
                
                if (existingPlayers.isEmpty()) {
                    mediaJpaRepository.save(score);
                }
            }
        }
    }
}