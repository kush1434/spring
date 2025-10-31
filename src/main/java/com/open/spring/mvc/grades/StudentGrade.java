package com.open.spring.mvc.grades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a student grade entry
 * Stores individual grade records with category, submodule, and student information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student_grades")
public class StudentGrade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private Integer submodule;
    
    @Column(length = 500)
    private String permalink;
    
    @Column(nullable = false)
    private String team;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String studentName;
    
    @Column(nullable = false)
    private Integer grade;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 1000)
    private String note;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructor without id (for creating new records)
    public StudentGrade(String category, Integer submodule, String permalink, String team, 
                       String status, String studentName, Integer grade, 
                       LocalDateTime timestamp, String note) {
        this.category = category;
        this.submodule = submodule;
        this.permalink = permalink;
        this.team = team;
        this.status = status;
        this.studentName = studentName;
        this.grade = grade;
        this.timestamp = timestamp;
        this.note = note;
    }
}

