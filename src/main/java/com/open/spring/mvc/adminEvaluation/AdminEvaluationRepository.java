package com.open.spring.mvc.adminEvaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminEvaluationRepository extends JpaRepository<AdminEvaluation, Long> {
    // This interface is intentionally left blank. 
    // Default JPA methods are used for database operations.
}
