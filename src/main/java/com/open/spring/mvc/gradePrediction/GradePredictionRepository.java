package com.open.spring.mvc.gradePrediction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradePredictionRepository extends JpaRepository<GradePrediction, Long> {
}
