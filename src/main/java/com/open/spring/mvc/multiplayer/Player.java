package com.open.spring.mvc.multiplayer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String status; // "online" or "offline"

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    // Constructor for quick creation
    public Player(String username, String status) {
        this.username = username;
        this.status = status;
        this.lastActive = LocalDateTime.now();
        this.connectedAt = LocalDateTime.now();
    }
}