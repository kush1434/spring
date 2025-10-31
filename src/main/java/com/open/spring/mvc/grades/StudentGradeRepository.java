package com.open.spring.mvc.grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for StudentGrade entity
 * Provides database operations and custom queries
 */
@Repository
public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {
    
    // Find all grades for a specific student, ordered by timestamp
    List<StudentGrade> findByStudentNameOrderByTimestampDesc(String studentName);
    
    // Find all grades by category
    List<StudentGrade> findByCategoryOrderByStudentName(String category);
    
    // Find grades by student name and category
    List<StudentGrade> findByStudentNameAndCategory(String studentName, String category);
    
    // Find all unique student names
    @Query("SELECT DISTINCT g.studentName FROM StudentGrade g ORDER BY g.studentName")
    List<String> findAllDistinctStudentNames();
    
    // Find all grades for a specific student and category, ordered by submodule
    List<StudentGrade> findByStudentNameAndCategoryOrderBySubmodule(String studentName, String category);
    
    // Get average grade for a student
    @Query("SELECT AVG(g.grade) FROM StudentGrade g WHERE g.studentName = :studentName")
    Double getAverageGradeByStudent(@Param("studentName") String studentName);
    
    // Get average grade for a student by category
    @Query("SELECT AVG(g.grade) FROM StudentGrade g WHERE g.studentName = :studentName AND g.category = :category")
    Double getAverageGradeByStudentAndCategory(@Param("studentName") String studentName, @Param("category") String category);
}

