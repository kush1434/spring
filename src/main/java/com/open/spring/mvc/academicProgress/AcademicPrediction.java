package com.open.spring.mvc.academicProgress;

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
public class AcademicPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double assignmentCompletionRate;
    private Double averageAssignmentScore;
    private Double collegeboardQuizAverage;
    private Integer officeHoursVisits;
    private Integer conduct;
    private Integer workHabit;
    private Integer githubContributions;
    private Double predictedScore;
    private Long createdAt;

    public AcademicPrediction(Double assignmentCompletionRate, Double averageAssignmentScore, 
                            Double collegeboardQuizAverage, Integer officeHoursVisits, 
                            Integer conduct, Integer workHabit, Integer githubContributions, 
                            Double predictedScore) {
        this.assignmentCompletionRate = assignmentCompletionRate;
        this.averageAssignmentScore = averageAssignmentScore;
        this.collegeboardQuizAverage = collegeboardQuizAverage;
        this.officeHoursVisits = officeHoursVisits;
        this.conduct = conduct;
        this.workHabit = workHabit;
        this.githubContributions = githubContributions;
        this.predictedScore = predictedScore;
        this.createdAt = System.currentTimeMillis();
    }
}
