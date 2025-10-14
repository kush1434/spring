package com.open.spring.mvc.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for accessing QuizScore data from the database.
 */
public interface QuizScoreRepository extends JpaRepository<QuizScore, Long> {

    /**
     * Retrieve all quiz scores across all users and whatnot,
     * ordered by highest score first, then earliest creation time.
     *
     * @return sorted list of QuizScore objects
     */
    @Query("SELECT q FROM QuizScore q ORDER BY q.score DESC, q.createdAt ASC")
    List<QuizScore> findAllOrderByScoreDesc();

    /**
     * Retrieve quiz scores for a specific user (case-insensitive),
     * ordered by highest score first.
     *
     * @param username the username to search for
     * @return list of QuizScore objects for that user
     */
    List<QuizScore> findByUsernameIgnoreCaseOrderByScoreDesc(@Param("username") String username);
}