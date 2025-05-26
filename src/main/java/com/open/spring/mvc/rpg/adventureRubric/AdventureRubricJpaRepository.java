package com.open.spring.mvc.rpg.adventureRubric;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AdventureRubricJpaRepository extends JpaRepository<AdventureRubric, Long> {
    AdventureRubric findByRuid(String ruid);
}