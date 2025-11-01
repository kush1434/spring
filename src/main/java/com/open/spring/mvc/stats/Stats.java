package com.open.spring.mvc.stats;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true) // Usernames should be unique
    private String username;

    private double frontend; //completion percentage /100
    private double backend; //completion percentage /100
    private double data; //completion percentage /100
    private double resume; //completion percentage /100
    private double ai; //completion percentage /100

    public Stats(String username, double frontend, double backend, double data, double resume, double ai) {
        this.username = username;
        this.frontend = frontend;
        this.backend = backend;
        this.data = data;
        this.resume = resume;
        this.ai = ai;
    }
}