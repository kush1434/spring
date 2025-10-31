package com.open.spring.mvc.grades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for grade queries
 * Used to return organized grade data to the frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradesResponse {
    
    private String message;
    private Integer totalGradesSaved;
    private Integer totalStudents;
    private List<String> studentNames;
    private Map<String, StudentSummary> studentSummaries;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSummary {
        private String studentName;
        private Integer totalGrades;
        private Double averageGrade;
        private List<CategoryGrade> gradesByCategory;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryGrade {
        private String category;
        private Integer gradeCount;
        private Double averageGrade;
    }
}

