package com.open.spring.mvc.leaderboard;

import java.util.ArrayList;

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

    private String role = "PLAYER";
    private boolean enabled = true;
    
    @Column(nullable = false)
    private int highScore = 0;

    public Gamer(String username, String password, String role, boolean enabled, int highScore) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.highScore = highScore;
    }

    public static Gamer createPlayer(String username, String password, String role, boolean enabled, int highScore) {
        Gamer player = new Gamer();
        player.setUsername(username);
        player.setPassword(password);
        player.setRole(role);
        player.setEnabled(enabled);
        player.setHighScore(highScore);
        return player;
    }

    public static Gamer[] init() {
        ArrayList<Gamer> players = new ArrayList<>();
        
        players.add(createPlayer("Avika", "pass123", "PLAYER", true, 15000));
        players.add(createPlayer("Soni", "pass456", "PLAYER", true, 28500));
        players.add(createPlayer("Nora", "pass789", "PLAYER", true, 42000));
        players.add(createPlayer("Gurshawn", "pass321", "PLAYER", true, 8500));
        players.add(createPlayer("Xavier", "pass654", "PLAYER", true, 55000));
        players.add(createPlayer("Spencer", "pass765", "PLAYER", true, 54000));

        return players.toArray(new Gamer[0]);
    }
}