package com.nighthawk.spring_portfolio.mvc.assignments;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AssignmentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Person student;

    private String content;
    private Double grade;
    private String feedback;

    private String comment;

    private long assignmentid;
    
    public AssignmentSubmission(Assignment assignment, Person student, String content, String comment) {
        this.assignment = assignment;
        this.student = student;
        this.content = content;
        this.grade = null;
        this.feedback = null;
        this.comment = comment;
        this.assignmentid=assignment.getId();
    }

    // Getters and Setters (if not using Lombok)
    public Long getId() {
        return id;
    }

    public Long getAssignmentId1() {
        return assignmentid;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public Person getStudent() {
        return student;
    }

    public String getContent() {
        return content;
    }

    public String getComment() {
        return comment;
    }

    public String getFeedback() {
        return feedback;
    }

    // Getter for assignment_id (foreign key column)
    public Long getAssignmentId2() {
        return assignment != null ? assignment.getId() : null;
    }

    public Double getGrade() {
        return grade;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
    
}