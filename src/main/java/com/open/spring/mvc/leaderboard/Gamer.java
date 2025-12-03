package com.open.spring.mvc.leaderboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Gamer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role;
    private boolean enabled;
    
    @Column(nullable = false)
    private int highScore;

    // Starting players for initialization
    public static String[] init() {
        final String[] playersArray = {
            "Avika",
            "Soni", 
            "Nora",
            "Gurshawn",
            "Xavier",
            "Spencer"
        };
        return playersArray;
    }
}