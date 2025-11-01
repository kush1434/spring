package com.open.spring.mvc.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StatsRepository extends JpaRepository<Stats, Long> {
    /**
     * Finds a Stats entry by username.
     * We use Optional in case the user doesn't exist.
     */
    Optional<Stats> findByUsername(String username);
}