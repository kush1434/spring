package com.open.spring.mvc.leaderboard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GamerJpaRepository extends JpaRepository<Gamer, Long> {
    Optional<Gamer> findByUsername(String username);
    List<Gamer> findByUsernameIgnoreCase(String username);
    Gamer findById(Integer playerId);
}