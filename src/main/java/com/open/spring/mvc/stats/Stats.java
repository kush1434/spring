package com.open.spring.mvc.stats;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // not added to db in backend

    @Column(unique = true) // Usernames should be unique
    private String username;

    private double frontend; //completion percentage /100
    private double backend; //completion percentage /100
    private double data; //completion percentage /100
    private double resume; //completion percentage /100
    private double ai; //completion percentage /100
}