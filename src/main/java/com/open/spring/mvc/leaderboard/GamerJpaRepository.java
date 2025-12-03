package com.open.spring.mvc.leaderboard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// JPA is an object-relational mapping (ORM) to persistent data, originally relational databases (SQL)
public interface GamerJpaRepository extends JpaRepository<Gamer, Long> {
    /* JPA has many built in methods: https://www.tutorialspoint.com/spring_boot_jpa/spring_boot_jpa_repository_methods.htm
    The below custom methods are prototyped for this application
    */
    Optional<Gamer> findByUsername(String username);
    List<Gamer> findByUsernameIgnoreCase(String username);
    List<Gamer> findAllByOrderByHighScoreDesc(); // Orders by high score descending
}