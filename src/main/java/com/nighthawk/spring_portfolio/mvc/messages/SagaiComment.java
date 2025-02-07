package com.nighthawk.spring_portfolio.mvc.messages;


import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaiComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(unique=true)
    private String content;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "message_id")
    private SagaiMessage message;
}