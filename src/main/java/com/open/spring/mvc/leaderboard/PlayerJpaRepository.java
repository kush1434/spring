package com.open.spring.mvc.leaderboard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerJpaRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUsername(String username);
    List<Player> findByUsernameIgnoreCase(String username);
    Player findById(Integer playerId);
}