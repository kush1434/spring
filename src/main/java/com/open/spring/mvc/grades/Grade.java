package com.open.spring.mvc.grades;

import jakarta.persistence.*;

@Entity
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String assignment;

    @Column(nullable = false)
    private Double score;

    // @Column(nullable = false)
    // private String gradeLevel;

    public Grade() {
    }

    public Grade(String studentId, String assignment, Double score, String gradeLevel) {
        this.studentId = studentId;
        this.assignment = assignment;
        this.score = score;
        // this.gradeLevel = gradeLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getAssignment() {
        return assignment;
    }

    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    // public String getGradeLevel() { return gradeLevel; }
    // public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel;
    // }

    /**
     * Alias for studentId to explicitly indicate this is the GitHub ID (uid) from
     * the Flask app.
     */
    public String getGithubId() {
        return studentId;
    }

    public void setGithubId(String githubId) {
        this.studentId = githubId;
    }
}
