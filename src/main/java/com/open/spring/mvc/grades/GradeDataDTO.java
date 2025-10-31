package com.open.spring.mvc.grades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for receiving grade collection data from the frontend
 * Matches the structure sent by the JavaScript grade collection system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDataDTO {
    
    private MetadataDTO metadata;
    private SummaryDTO summary;
    private Map<String, List<GradeEntryDTO>> gradesByCategory;
    private List<GradeEntryDTO> allGrades;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetadataDTO {
        private String collectionDate;
        private Integer totalSubmodules;
        private String baseUrl;
        private String mode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDTO {
        private Integer total;
        private Integer successful;
        private Integer notImplemented;
        private Integer errors;
        private Integer mock;
        private List<String> uniqueStudents;
        private Boolean usingMockData;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeEntryDTO {
        private String category;
        private Integer submodule;
        private String permalink;
        private String team;
        private String status;
        private String studentName;
        private Integer grade;
        private String timestamp;
        private String note;
        private String error;
    }
}

