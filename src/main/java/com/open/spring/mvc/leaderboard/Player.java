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
public class Player {
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

    public Player(String username, String password, String role, boolean enabled, int highScore) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.highScore = highScore;
    }

    public static Player createPlayer(String username, String password, String role, boolean enabled, int highScore) {
        Player player = new Player();
        player.setUsername(username);
        player.setPassword(password);
        player.setRole(role);
        player.setEnabled(enabled);
        player.setHighScore(highScore);
        return player;
    }

    public static Player[] init() {
        ArrayList<Player> players = new ArrayList<>();
        
        players.add(createPlayer("ProGamer123", "pass123", "PLAYER", true, 15000));
        players.add(createPlayer("SpeedRunner", "pass456", "PLAYER", true, 28500));
        players.add(createPlayer("ElitePlayer", "pass789", "PLAYER", true, 42000));
        players.add(createPlayer("NoobMaster", "pass321", "PLAYER", true, 8500));
        players.add(createPlayer("GamerGod", "pass654", "PLAYER", true, 55000));

        return players.toArray(new Player[0]);
    }
}