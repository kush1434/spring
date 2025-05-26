package com.open.spring.mvc.rpg.adventureGameStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameStatJpaRepository extends JpaRepository<GameStat, Long> {
    GameStat findByUid_uid(String uid);
}