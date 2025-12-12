package com.open.spring.mvc.gradePrediction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradePrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("student_id")
    private Long studentId;
    @JsonProperty("attendance")
    private Double attendance;
    @JsonProperty("work_habits")
    private Double workHabits;
    @JsonProperty("behavior")
    private Double behavior;
    @JsonProperty("timeliness")
    private Double timeliness;
    @JsonProperty("tech_sense")
    private Double techSense;
    @JsonProperty("tech_talk")
    private Double techTalk;
    @JsonProperty("tech_growth")
    private Double techGrowth;
    @JsonProperty("advocacy")
    private Double advocacy;
    @JsonProperty("communication")
    private Double communication;
    @JsonProperty("integrity")
    private Double integrity;
    @JsonProperty("organization")
    private Double organization;
    @JsonProperty("final_grade")
    private Double predictedScore;
    private Long createdAt;

    public GradePrediction(Double attendance, Double workHabits, Double behavior, Double timeliness,
                           Double techSense, Double techTalk, Double techGrowth, Double advocacy,
                           Double communication, Double integrity, Double organization, Double predictedScore) {
        this.attendance = attendance;
        this.workHabits = workHabits;
        this.behavior = behavior;
        this.timeliness = timeliness;  
        this.techSense = techSense;
        this.techTalk = techTalk;
        this.techGrowth = techGrowth;
        this.advocacy = advocacy;
        this.communication = communication;
        this.integrity = integrity;
        this.organization = organization;
        this.predictedScore = predictedScore;
        this.createdAt = System.currentTimeMillis();
    }
}
