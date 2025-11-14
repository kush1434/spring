package com.open.spring.mvc.rpg.adventure;

import java.time.LocalDateTime;

// imports trimmed to used ones
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "adventure")
public class Adventure {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long personId;
    private String personUid;

    private Long questionId;
    private String questionTitle;
    @Lob
    private String questionContent;
    private String questionCategory;
    private Integer questionPoints;

    private Long choiceId;
    private String choiceText;
    private Boolean choiceIsCorrect;

    private Boolean answerIsCorrect;
    @Lob
    private String answerContent;
    private Long chatScore;

    private String rubricRuid;
    @Lob
    private String rubricCriteria;

    private Double balance;

    private LocalDateTime createdAt;
}
