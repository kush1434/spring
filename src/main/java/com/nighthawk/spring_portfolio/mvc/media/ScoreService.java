package com.nighthawk.spring_portfolio.mvc.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.ArrayList;

import com.nighthawk.spring_portfolio.mvc.media.Score;

@Service
public class ScoreService {
    
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
            List<Score> existingPlayers = mediaJpaRepository.findByPersonName(score.getPersonName());
            
            if (existingPlayers.isEmpty()) {
                mediaJpaRepository.save(score);
            }
        }
    }
}
