package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.synergy.SynergyGrade;
import com.nighthawk.spring_portfolio.mvc.synergy.SynergyGradeJpaRepository;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

/**
 * REST API Controller for managing assignment submissions.
 * Provides endpoints for CRUD operations on assignment submissions.
 */
@RestController
@RequestMapping("/api/submissions")
public class AssignmentSubmissionAPIController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AssignmentSubmissionJPA submissionRepo;

    @Autowired
    private AssignmentJpaRepository assignmentRepo;

    @Autowired
    private PersonJpaRepository personRepo;

    @Autowired
    private SynergyGradeJpaRepository gradesRepo;

    @Getter
    @Setter
    public static class PersonSubmissionDto {
        public Long id;
        public String name;
        public String email;
        public String uid;

        public PersonSubmissionDto(Person person) {
            this.id = person.getId();
            this.name = person.getName();
            this.email = person.getEmail();
            this.uid = person.getUid();
        }
    }
    
    @Getter
    @Setter
    public static class AssignmentReturnDto {
        public Long id;
        public String name;
        public String type;
        public String description;
        public Double points;
        public String dueDate;
        public String timestamp;

        public AssignmentReturnDto(Assignment assignment) {
            this.id = assignment.getId();
            this.name = assignment.getName();
            this.type = assignment.getType();
            this.description = assignment.getDescription();
            this.points = assignment.getPoints();
            this.dueDate = assignment.getDueDate();
            this.timestamp = assignment.getTimestamp();
        }
    }

    /**
     * Get all submissions for a specific student.
     * 
     * @param studentId the ID of the student whose submissions are to be fetched
     * @return a ResponseEntity containing a list of submissions for the given student ID
     */
    @Transactional
    @GetMapping("/getSubmissions/{studentId}")
    public ResponseEntity<?> getSubmissions(@PathVariable Long studentId) {
        List<AssignmentSubmissionReturnDto> submissions = submissionRepo.findByStudentId(studentId).stream()
        .map(AssignmentSubmissionReturnDto::new)
        .toList();;
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    /**
     * Create a new assignment submission.
     * 
     * @param submission the AssignmentSubmission object to be created
     * @return a ResponseEntity containing the created submission and HTTP status CREATED
     */
    @PostMapping("/Submit")
    public ResponseEntity<AssignmentSubmission> createAssignment(@RequestBody AssignmentSubmission submission) {
        submissionRepo.save(submission);
        return new ResponseEntity<>(submission, HttpStatus.CREATED);
    }

    @Getter
    @Setter
    public static class SubmissionRequestDto {
        public Long assignmentId;
        public List<Long> studentIds;
        public String content;
        public String comment;
        public Boolean isLate;
    }

    /**
     * Submit an assignment for a student.
     * 
     * @param assignmentId the ID of the assignment being submitted
     * @param studentId    the ID of the student submitting the assignment
     * @param content      the content of the submission
     * @param comment      any comments related to the submission
     * @return a ResponseEntity containing the created submission or an error if the assignment is not found
     */
    @PostMapping("/submit/{assignmentId}")
    public ResponseEntity<?> submitAssignment(
            @RequestBody SubmissionRequestDto requestData
    ) {
        Assignment assignment = assignmentRepo.findById(requestData.assignmentId).orElse(null);
        List<Person> students = personRepo.findAllById(requestData.studentIds);
        if (assignment != null) {
            AssignmentSubmission submission = new AssignmentSubmission(assignment, students, requestData.content, requestData.comment,requestData.isLate);
            AssignmentSubmission savedSubmission = submissionRepo.save(submission);
            return new ResponseEntity<>(new AssignmentSubmissionReturnDto(savedSubmission), HttpStatus.CREATED);
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Assignment not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Grade an existing assignment submission.
     * 
     * @param submissionId the ID of the submission to be graded
     * @param grade        the grade to be assigned to the submission
     * @param feedback     optional feedback for the submission
     * @return a ResponseEntity indicating success or an error if the submission is not found
     */
    @PostMapping("/grade/{submissionId}")
    @Transactional
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestParam Double grade,
            @RequestParam(required = false) String feedback
    ) {
        AssignmentSubmission submission = submissionRepo.findById(submissionId).orElse(null);
        if (submission == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Submission not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);    
        }

        // we have a correct submission
        submission.setGrade(grade);
        submission.setFeedback(feedback);
        submissionRepo.save(submission);

        for (Person student : submission.getStudents()) {
            SynergyGrade assignedGrade = gradesRepo.findByAssignmentAndStudent(submission.getAssignment(), student);
            if (assignedGrade != null) {
                // the assignment has a previously assigned grade, so we are just updating it
                assignedGrade.setGrade(grade);
                gradesRepo.save(assignedGrade);
            }
            else {
                // assignment is not graded, we must create a new grade
                SynergyGrade newGrade = new SynergyGrade(grade, submission.getAssignment(), student);
                gradesRepo.save(newGrade);
            }
        }

        return new ResponseEntity<>("Grade updated successfully", HttpStatus.OK);
    }

    /**
     * Get all submissions for a specific assignment.
     * 
     * @param assignmentId the ID of the assignment whose submissions are to be fetched
     * @return a ResponseEntity containing a list of submissions or an error if the assignment is not found
     */
    @Transactional
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        String uid = userDetails.getUsername();
        Person user = personRepo.findByUid(uid);
        if (user == null) {
            logger.error("User not found with email: {}", uid);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with uid: " + uid);
        }

        Assignment assignment = assignmentRepo.findById(assignmentId).orElse(null);
        if (assignment == null) {
            return new ResponseEntity<>(
                Collections.singletonMap("error", "Assignment not found"), 
                HttpStatus.NOT_FOUND
            );
        }

        List<AssignmentSubmission> submissions = submissionRepo.findByAssignmentId(assignmentId);
        List<AssignmentSubmissionReturnDto> submissionsReturn;

        if (!(user.hasRoleWithName("ROLE_TEACHER") || user.hasRoleWithName("ROLE_ADMIN"))) {
            // if they aren't a teacher or admin, only let them see submissions they are assigned to grade
            submissionsReturn = submissions.stream()
                .filter(submission -> submission.getAssignedGraders().contains(user))
                .map(AssignmentSubmissionReturnDto::new)
                .collect(Collectors.toList());
        } else {
            submissionsReturn = submissions.stream()
                .map(AssignmentSubmissionReturnDto::new)
                .collect(Collectors.toList());
        }
    
        return new ResponseEntity<>(submissionsReturn, HttpStatus.OK);
    }

    @PostMapping("/{id}/assigned-graders")
    public ResponseEntity<?> assignGradersToSubmission(@PathVariable Long id, @RequestBody List<Long> personIds) {
        Optional<AssignmentSubmission> submissionOptional = submissionRepo.findById(id);
        if (!submissionOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found");
        }

        AssignmentSubmission submission = submissionOptional.get();
        List<Person> persons = personRepo.findAllById(personIds);

        submission.setAssignedGraders(persons);

        submissionRepo.save(submission);
        return ResponseEntity.ok("Persons assigned successfully");
    }

    @GetMapping("/{id}/assigned-graders")
    public ResponseEntity<?> getAssignedGraders(@PathVariable Long id) {
        Optional<AssignmentSubmission> submissionOptional = submissionRepo.findById(id);
        if (!submissionOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Submission not found");
        }

        AssignmentSubmission submission = submissionOptional.get();
        List<Person> assignedGraders = submission.getAssignedGraders();
        
        // Return just the IDs of assigned persons
        List<Long> assignedGraderIds = assignedGraders.stream()
            .map(Person::getId)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(assignedGraderIds);
    }
}
