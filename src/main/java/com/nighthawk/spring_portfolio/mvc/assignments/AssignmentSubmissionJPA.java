package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nighthawk.spring_portfolio.mvc.person.Person;

@Repository
public interface AssignmentSubmissionJPA extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
    List<AssignmentSubmission> findByAssignedGraders(Person grader);

    // Note: This used to just be a method signature, but now there's a query too because students is now many to many instead of many to one
    @Query("SELECT a FROM AssignmentSubmission a JOIN a.students s WHERE s.id = :studentId")
    List<AssignmentSubmission> findByStudentId(Long studentId);    
}