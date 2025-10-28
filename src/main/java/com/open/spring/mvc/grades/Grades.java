package com.open.spring.mvc.grades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Grades {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String uid;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(nullable = false, length = 4000)
    private String response;

    @Column(nullable = false)
    private double grade; // value between 0.55 and 1.0

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Convenience constructor
    public Grades(String uid, String question, String response, double grade) {
        this.uid = uid;
        this.question = question;
        this.response = response;
        this.grade = grade;
        this.createdAt = LocalDateTime.now();
    }
}
