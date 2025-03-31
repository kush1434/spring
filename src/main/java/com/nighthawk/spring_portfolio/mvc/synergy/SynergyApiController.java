package com.nighthawk.spring_portfolio.mvc.synergy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nighthawk.spring_portfolio.mvc.assignments.Assignment;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentJpaRepository;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;

import jakarta.validation.Valid;
import lombok.Getter;

@RestController
@RequestMapping("/api/synergy")
public class SynergyApiController {
    @Autowired
    private SynergyGradeJpaRepository gradeRepository;

    @Autowired
    private SynergyGradeRequestJpaRepository gradeRequestRepository;

    @Autowired
    private AssignmentJpaRepository assignmentRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    /**
     * A data transfer object that stores information about a grade request.
     */
    @Getter
    public static class SynergyGradeRequestDto {
        private Long studentId;
        private Long assignmentId;
        private Double gradeSuggestion;
        private String explanation;
    }
    
    /**
     * A dto that stores information about a grade request for many students.
     */
    @Getter
    public static class SynergyGradeRequestDtoBulk {
        private List<Long> studentIds;
        private Long assignmentId;
        private Double gradeSuggestion;
        private String explanation;
    }
    
    /**
     * A data transfer object that stores information about a grade request that is made for
     * the student who creaed it.
     */
    @Getter
    public static class SynergyGradeRequestSelfDto {
        public Long assignmentId;
        public Double gradeSuggestion;
        public String explanation;
    }
    
    /**
     * A data transfer object that stores information about a grade request that is made for
     * the student who creaed it.
     */
    @Getter
    public static class SynergyGradeRequestSeedDto {
        public Double gradeSuggestion;
        public String explanation;
    }

    /**
     * A data transfer object that stores the id of a grade request.
     */
    @Getter
    public static class SynergyGradeRequestIdDto {
        private Long requestId;
    }

    @Getter
    public static class SynergyGradeDto {
        private Long id;
        private Double grade;
        private Long assignmentId;
        private Long studentId;

        public SynergyGradeDto(SynergyGrade grade) {
            this.id = grade.getId();
            this.grade = grade.getGrade();
            this.assignmentId = grade.getAssignment().getId();
            this.studentId = grade.getStudent().getId();
        }
    }

    /**
     * A POST endpoint to save a single grade to the database.
     * @param grade The parameters for a Grade POJO, passed in as JSON.
     * @return A JSON object confirming that the grade was saved.
     */
    @PostMapping("/grade")
    public ResponseEntity<Map<String, String>> updateGrade(@RequestBody SynergyGrade grade) {
        gradeRepository.save(grade);
        return ResponseEntity.ok(Map.of("message", "Successfully saved this grade."));
    }
    
    /**
     * A GET endpoint to retrieve all the grades.
     * @return A JSON object containing all the grades.
     */
    @GetMapping("/grades")
    public ResponseEntity<?> getGrades() {
        List<SynergyGradeDto> grades = new ArrayList<>();
        for (SynergyGrade grade : gradeRepository.findAll()) {
            grades.add(new SynergyGradeDto(grade));
        }
        return ResponseEntity.ok(grades);
    }
    
    /**
     * A POST endpoint to update many grades in bulk.
     * @param grades A formdata which is a map of strings of format grades[ASSIGNMENT_ID][STUDENT_ID] to numerical grades (or empty strings if there is no grade yet)
     * @return A JSON object confirming that the grades were updated
     */
    @PostMapping("/grades")
    public ResponseEntity<Map<String, String>> updateAllGrades(@RequestParam Map<String, String> grades) throws ResponseStatusException {
        for (String key : grades.keySet()) {
            // only actually look at relevant parameters
            if (!key.matches("grades\\[\\d+\\]\\[\\d+\\]")) {
                continue;
            }
        
            String[] ids = key.replace("grades[", "").replace("]", "").split("\\[");
            Long assignmentId = Long.parseLong(ids[0]);
            Long studentId = Long.parseLong(ids[1]);
            String gradeValueStr = grades.get(key);

            if (isNumeric(gradeValueStr)) { // otherwise, we have an empty string so ignore it
                Double gradeValue = Double.parseDouble(gradeValueStr);
                SynergyGrade grade = gradeRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).orElse(null);

                if (grade == null) {
                    Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> 
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment ID passed")
                    );
                    Person student = personRepository.findById(studentId).orElseThrow(() -> 
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student ID passed")
                    );
    
                    grade = new SynergyGrade();
                    grade.setAssignment(assignment);
                    grade.setStudent(student);
                }
                grade.setGrade(gradeValue);
                gradeRepository.save(grade);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Successfully updated the grades."));
    }
    
    /**
     * A POST endpoint to create a grade request.
     * @param userDetails The information about the logged in user. Automatically passed in by thymeleaf.
     * @param requestData The JSON data passed in, of the format studentId: Long, assignmentId: Long,
     *                    gradeSuggestion: Double, explanation: String
     * @return A JSON object signifying that the request was created.
     */
    @PostMapping("/grades/requests")
    public ResponseEntity<Map<String, String>> createGradeRequest(
        @AuthenticationPrincipal UserDetails userDetails, 
        @RequestBody SynergyGradeRequestDto requestData
    ) throws ResponseStatusException {
        String uid = userDetails.getUsername();
        Person grader = personRepository.findByUid(uid);
        if (grader == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You must be a logged in user to do this"
            );
        }

        Person student = personRepository.findById(requestData.getStudentId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student ID passed")
        );
        Assignment assignment = assignmentRepository.findById(requestData.getAssignmentId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment ID passed")
        );;
        
        SynergyGradeRequest gradeRequest = new SynergyGradeRequest(assignment, student, grader, requestData.getExplanation(), requestData.getGradeSuggestion());
        gradeRequestRepository.save(gradeRequest);

        return ResponseEntity.ok(Map.of("message", "Successfully created the grade request."));
    }
    
    
    /**
     * A POST endpoint to create multiple grade requests.
     * @param userDetails The information about the logged in user. Automatically passed in by thymeleaf.
     * @param requestData The JSON data passed in, of the format studentId: Long, assignmentId: Long,
     *                    gradeSuggestion: Double, explanation: String
     * @return A JSON object signifying that the request was created.
     */
    @PostMapping("/grades/requests/bulk")
    public ResponseEntity<Map<String, String>> createGradeRequest(
        @AuthenticationPrincipal UserDetails userDetails, 
        @RequestBody SynergyGradeRequestDtoBulk requestData
    ) throws ResponseStatusException {
        String uid = userDetails.getUsername();
        Person grader = personRepository.findByUid(uid);
        if (grader == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You must be a logged in user to do this"
            );
        }

        List<Person> students = personRepository.findAllById(requestData.getStudentIds());
        if (students.isEmpty()) {
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student ID passed");
        }

        Assignment assignment = assignmentRepository.findById(requestData.getAssignmentId()).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment ID passed")
        );
        
        for (Person student : students) {
            SynergyGradeRequest gradeRequest = new SynergyGradeRequest(assignment, student, grader, requestData.getExplanation(), requestData.getGradeSuggestion());
            gradeRequestRepository.save(gradeRequest);
        }

        return ResponseEntity.ok(Map.of("message", "Successfully created the grade requests."));
    }

    @GetMapping("/grades/requests/seed")
    public ResponseEntity<?> getGradeRequestsSeed(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Person student = personRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You must be a logged in user to do this"
            );
        }

        return ResponseEntity.ok(gradeRequestRepository.findByStudentId(student.getId()));
    }

    /**
     * A POST endpoint to create a grade request for seed.
     * @param userDetails The information about the logged in user. Automatically passed in by thymeleaf.
     * @param requestData The JSON data passed in, of the format studentId: Long, assignmentId: Long,
     *                    gradeSuggestion: Double, explanation: String
     * @return A JSON object signifying that the request was created.
     */
    @PostMapping("/grades/requests/seed")
    public ResponseEntity<Map<String, String>> createGradeRequestSeed(
        @AuthenticationPrincipal UserDetails userDetails, 
        @RequestBody SynergyGradeRequestSeedDto requestData
    ) throws ResponseStatusException {
        String uid = userDetails.getUsername();
        Person student = personRepository.findByUid(uid);
        if (student == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You must be a logged in user to do this"
            );
        }

        Assignment assignment = assignmentRepository.findByName("Seed");
        if (assignment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no seed assignment");
        }
        
        SynergyGradeRequest gradeRequest = new SynergyGradeRequest(assignment, student, student, requestData.getExplanation(), requestData.getGradeSuggestion());
        gradeRequestRepository.save(gradeRequest);

        return ResponseEntity.ok(Map.of("message", "Successfully created the grade request for seed."));
    }

    /**
     * A POST endpoint to create a grade request.
     * @param userDetails The information about the logged in user. Automatically passed in by thymeleaf.
     * @param requestData The JSON data passed in, of the format studentId: Long, assignmentId: Long,
     *                    gradeSuggestion: Double, explanation: String
     * @return A JSON object signifying that the request was created.
     */
    @PostMapping("/grades/requests/self")
    public ResponseEntity<Map<String, String>> createGradeRequestForSelf(
        @AuthenticationPrincipal UserDetails userDetails, 
        @RequestBody SynergyGradeRequestSelfDto requestData
    ) throws ResponseStatusException {
        String uid = userDetails.getUsername();
        Person student = personRepository.findByUid(uid);
        if (student == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You must be a logged in user to do this"
            );
        }

        Assignment assignment = assignmentRepository.findById(requestData.assignmentId).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment ID passed")
        );;
        
        SynergyGradeRequest gradeRequest = new SynergyGradeRequest(assignment, student, student, requestData.explanation, requestData.gradeSuggestion);
        gradeRequestRepository.save(gradeRequest);

        return ResponseEntity.ok(Map.of("message", "Successfully created the grade request."));
    }


    /**
     * A POST endpoint to accept a grade request.
     * @param body The JSON data passed in, of the format requestId: Long
     * @return A JSON object signifying that the request was accepted.
     */
    @PostMapping("/grade/requests/accept")
    public ResponseEntity<Map<String, String>> acceptRequest(@Valid @RequestBody SynergyGradeRequestIdDto body) throws ResponseStatusException {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request body");
        }

        SynergyGradeRequest request = gradeRequestRepository.findById((long) body.getRequestId()).orElse(null);
        if (request == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Grade request not found"
            );
        }
        else if (request.isAccepted()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Grade request was already accepted before"
            );
        }

        Assignment assignment = request.getAssignment();
        Person student = request.getStudent();
        SynergyGrade grade = gradeRepository.findByAssignmentAndStudent(assignment, request.getStudent());
        if (grade == null) {
            grade = new SynergyGrade();
            grade.setAssignment(assignment);
            grade.setStudent(student);
        }
        grade.setGrade(request.getGradeSuggestion());
        gradeRepository.save(grade);

        request.accept();
        gradeRequestRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Successfully accepted the grade request."));
    }

    /**
     * Rejects a grade request.
     * @param body The JSON data passed in, of the format requestId: Long
     * @return A JSON object signifying that the request was rejected.
     */
    @PostMapping("/grade/requests/reject")
    public ResponseEntity<Map<String, String>> rejectRequest(@RequestBody SynergyGradeRequestIdDto body) throws ResponseStatusException {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request body.");
        }
    
        SynergyGradeRequest request = gradeRequestRepository.findById((long) body.getRequestId()).orElse(null);
        if (request == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Grade request not found."
            );
        }
        else if (request.isAccepted()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Grade request was already accepted before."
            );
        }

        request.reject();
        gradeRequestRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Successfully rejected the grade request."));
    }

    @GetMapping("/grades/map/{userId}")
    public ResponseEntity<Map<Long, Double>> getStudentGradesAsMap(@PathVariable Long userId) {
        Person student = personRepository.findById(userId).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found")
        );
        
        List<SynergyGrade> studentGrades = gradeRepository.findByStudent(student);
        
        Map<Long, Double> assignmentGrades = studentGrades.stream()
            .collect(Collectors.toMap(
                grade -> grade.getAssignment().getId(),
                SynergyGrade::getGrade,
                (existing, replacement) -> existing
            ));
        
        return new ResponseEntity<>(assignmentGrades, HttpStatus.OK);
    }

    /**
     * Returns whether or not a string is numeric or not (can be a decimal)
     * @param str A string
     * @return A boolean indicating that the string is or is not numeric
     */
    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

}