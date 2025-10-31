package com.open.spring.mvc.grades;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for handling student grade operations
 * Provides endpoints for receiving grade data from JavaScript and querying stored grades
 */
@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*") // Allow requests from frontend
public class StudentGradeController {
    
    @Autowired
    private StudentGradeService gradeService;
    
    /**
     * Main endpoint to receive grade collection data from JavaScript
     * Accepts the complete JSON structure sent by CSPortfolioGrades
     * 
     * @param gradeData Complete grade collection data
     * @return Response with summary of saved grades organized by student
     */
    @PostMapping("/submit")
    public ResponseEntity<StudentGradesResponse> submitGrades(@RequestBody GradeDataDTO gradeData) {
        try {
            StudentGradesResponse response = gradeService.processAndSaveGrades(gradeData);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            // Return error response
            StudentGradesResponse errorResponse = new StudentGradesResponse(
                "Error saving grades: " + e.getMessage(),
                0, 0, null, null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all grades for a specific student
     * 
     * @param studentName The name of the student
     * @return List of all grades for that student
     */
    @GetMapping("/student/{studentName}")
    public ResponseEntity<List<StudentGrade>> getStudentGrades(@PathVariable String studentName) {
        List<StudentGrade> grades = gradeService.getGradesByStudent(studentName);
        if (grades.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }
    
    /**
     * Get detailed summary for a specific student
     * 
     * @param studentName The name of the student
     * @return Summary with averages and category breakdown
     */
    @GetMapping("/student/{studentName}/summary")
    public ResponseEntity<StudentGradesResponse.StudentSummary> getStudentSummary(@PathVariable String studentName) {
        StudentGradesResponse.StudentSummary summary = gradeService.getStudentSummary(studentName);
        if (summary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
    
    /**
     * Get all grades for a specific category
     * 
     * @param category The category (frontend, backend, etc.)
     * @return List of all grades in that category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<StudentGrade>> getGradesByCategory(@PathVariable String category) {
        List<StudentGrade> grades = gradeService.getGradesByCategory(category);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }
    
    /**
     * Get all unique student names
     * 
     * @return List of all student names in the database
     */
    @GetMapping("/students")
    public ResponseEntity<List<String>> getAllStudents() {
        List<String> students = gradeService.getAllStudentNames();
        return new ResponseEntity<>(students, HttpStatus.OK);
    }
    
    /**
     * Get all grades organized by student name
     * 
     * @return Map of student names to their grade lists
     */
    @GetMapping("/by-student")
    public ResponseEntity<Map<String, List<StudentGrade>>> getAllGradesByStudent() {
        Map<String, List<StudentGrade>> gradesByStudent = gradeService.getAllGradesGroupedByStudent();
        return new ResponseEntity<>(gradesByStudent, HttpStatus.OK);
    }
    
    /**
     * Get average grade for a student
     * 
     * @param studentName The name of the student
     * @return The average grade
     */
    @GetMapping("/student/{studentName}/average")
    public ResponseEntity<Map<String, Object>> getStudentAverage(@PathVariable String studentName) {
        Double average = gradeService.getStudentAverageGrade(studentName);
        if (average == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("studentName", studentName);
        response.put("averageGrade", Math.round(average * 100.0) / 100.0);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Get average grade for a student in a specific category
     * 
     * @param studentName The name of the student
     * @param category The category
     * @return The average grade for that category
     */
    @GetMapping("/student/{studentName}/category/{category}/average")
    public ResponseEntity<Map<String, Object>> getStudentCategoryAverage(
            @PathVariable String studentName, 
            @PathVariable String category) {
        
        Double average = gradeService.getStudentCategoryAverage(studentName, category);
        if (average == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("studentName", studentName);
        response.put("category", category);
        response.put("averageGrade", Math.round(average * 100.0) / 100.0);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Health check endpoint
     * 
     * @return Simple status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Student Grade API");
        response.put("message", "Service is running");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

