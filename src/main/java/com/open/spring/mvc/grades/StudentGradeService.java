package com.open.spring.mvc.grades;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for handling student grade business logic
 * Processes grade data and provides query methods
 */
@Service
public class StudentGradeService {
    
    @Autowired
    private StudentGradeRepository gradeRepository;
    
    /**
     * Process and save grade collection data from frontend
     * @param gradeData The complete grade data structure from JavaScript
     * @return Response with summary of saved data
     */
    @Transactional
    public StudentGradesResponse processAndSaveGrades(GradeDataDTO gradeData) {
        List<StudentGrade> gradesToSave = new ArrayList<>();
        
        // Process all grades from the allGrades array
        if (gradeData.getAllGrades() != null) {
            for (GradeDataDTO.GradeEntryDTO entry : gradeData.getAllGrades()) {
                // Only save grades with valid student names and grade values
                if (entry.getStudentName() != null && entry.getGrade() != null) {
                    StudentGrade grade = convertDTOToEntity(entry);
                    gradesToSave.add(grade);
                }
            }
        }
        
        // Save all grades to database
        List<StudentGrade> savedGrades = gradeRepository.saveAll(gradesToSave);
        
        // Create response with student summaries
        return createResponse(savedGrades);
    }
    
    /**
     * Convert DTO to Entity
     */
    private StudentGrade convertDTOToEntity(GradeDataDTO.GradeEntryDTO dto) {
        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(dto.getTimestamp(), 
                DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            timestamp = LocalDateTime.now();
        }
        
        return new StudentGrade(
            dto.getCategory(),
            dto.getSubmodule(),
            dto.getPermalink(),
            dto.getTeam(),
            dto.getStatus(),
            dto.getStudentName(),
            dto.getGrade(),
            timestamp,
            dto.getNote()
        );
    }
    
    /**
     * Create response with student summaries organized by name
     */
    private StudentGradesResponse createResponse(List<StudentGrade> grades) {
        // Group grades by student name
        Map<String, List<StudentGrade>> gradesByStudent = grades.stream()
            .collect(Collectors.groupingBy(StudentGrade::getStudentName));
        
        // Create summaries for each student
        Map<String, StudentGradesResponse.StudentSummary> summaries = new HashMap<>();
        List<String> studentNames = new ArrayList<>();
        
        for (Map.Entry<String, List<StudentGrade>> entry : gradesByStudent.entrySet()) {
            String studentName = entry.getKey();
            List<StudentGrade> studentGrades = entry.getValue();
            
            studentNames.add(studentName);
            
            // Calculate overall average
            double avgGrade = studentGrades.stream()
                .mapToInt(StudentGrade::getGrade)
                .average()
                .orElse(0.0);
            
            // Group by category and calculate category averages
            Map<String, List<StudentGrade>> gradesByCategory = studentGrades.stream()
                .collect(Collectors.groupingBy(StudentGrade::getCategory));
            
            List<StudentGradesResponse.CategoryGrade> categoryGrades = new ArrayList<>();
            for (Map.Entry<String, List<StudentGrade>> catEntry : gradesByCategory.entrySet()) {
                String category = catEntry.getKey();
                List<StudentGrade> catGrades = catEntry.getValue();
                
                double catAvg = catGrades.stream()
                    .mapToInt(StudentGrade::getGrade)
                    .average()
                    .orElse(0.0);
                
                categoryGrades.add(new StudentGradesResponse.CategoryGrade(
                    category,
                    catGrades.size(),
                    Math.round(catAvg * 100.0) / 100.0
                ));
            }
            
            // Sort categories alphabetically
            categoryGrades.sort(Comparator.comparing(StudentGradesResponse.CategoryGrade::getCategory));
            
            summaries.put(studentName, new StudentGradesResponse.StudentSummary(
                studentName,
                studentGrades.size(),
                Math.round(avgGrade * 100.0) / 100.0,
                categoryGrades
            ));
        }
        
        // Sort student names alphabetically
        Collections.sort(studentNames);
        
        return new StudentGradesResponse(
            "Grades successfully saved and organized by student",
            grades.size(),
            studentNames.size(),
            studentNames,
            summaries
        );
    }
    
    /**
     * Get all grades for a specific student, ordered by timestamp
     */
    public List<StudentGrade> getGradesByStudent(String studentName) {
        return gradeRepository.findByStudentNameOrderByTimestampDesc(studentName);
    }
    
    /**
     * Get all grades for a specific category
     */
    public List<StudentGrade> getGradesByCategory(String category) {
        return gradeRepository.findByCategoryOrderByStudentName(category);
    }
    
    /**
     * Get all unique student names
     */
    public List<String> getAllStudentNames() {
        return gradeRepository.findAllDistinctStudentNames();
    }
    
    /**
     * Get average grade for a student
     */
    public Double getStudentAverageGrade(String studentName) {
        return gradeRepository.getAverageGradeByStudent(studentName);
    }
    
    /**
     * Get average grade for a student in a specific category
     */
    public Double getStudentCategoryAverage(String studentName, String category) {
        return gradeRepository.getAverageGradeByStudentAndCategory(studentName, category);
    }
    
    /**
     * Get all grades organized by student name
     */
    public Map<String, List<StudentGrade>> getAllGradesGroupedByStudent() {
        List<StudentGrade> allGrades = gradeRepository.findAll();
        return allGrades.stream()
            .collect(Collectors.groupingBy(StudentGrade::getStudentName));
    }
    
    /**
     * Get detailed summary for a specific student
     */
    public StudentGradesResponse.StudentSummary getStudentSummary(String studentName) {
        List<StudentGrade> grades = gradeRepository.findByStudentNameOrderByTimestampDesc(studentName);
        
        if (grades.isEmpty()) {
            return null;
        }
        
        // Calculate overall average
        double avgGrade = grades.stream()
            .mapToInt(StudentGrade::getGrade)
            .average()
            .orElse(0.0);
        
        // Group by category
        Map<String, List<StudentGrade>> gradesByCategory = grades.stream()
            .collect(Collectors.groupingBy(StudentGrade::getCategory));
        
        List<StudentGradesResponse.CategoryGrade> categoryGrades = new ArrayList<>();
        for (Map.Entry<String, List<StudentGrade>> entry : gradesByCategory.entrySet()) {
            String category = entry.getKey();
            List<StudentGrade> catGrades = entry.getValue();
            
            double catAvg = catGrades.stream()
                .mapToInt(StudentGrade::getGrade)
                .average()
                .orElse(0.0);
            
            categoryGrades.add(new StudentGradesResponse.CategoryGrade(
                category,
                catGrades.size(),
                Math.round(catAvg * 100.0) / 100.0
            ));
        }
        
        categoryGrades.sort(Comparator.comparing(StudentGradesResponse.CategoryGrade::getCategory));
        
        return new StudentGradesResponse.StudentSummary(
            studentName,
            grades.size(),
            Math.round(avgGrade * 100.0) / 100.0,
            categoryGrades
        );
    }
}

