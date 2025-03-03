package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentsApiController {

    @Autowired
    private AssignmentJpaRepository assignmentRepo;

    @Autowired
    private AssignmentSubmissionJPA submissionRepo;

    @Autowired
    private PersonJpaRepository personRepo;

    @Getter
    @Setter
    public static class AssignmentDto {
        public Long id;
        public String name;
        public String type;
        public String description;
        public Double points;
        public String dueDate;
        public String timestamp;

        public AssignmentDto(Assignment assignment) {
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
     * A POST endpoint to create an assignment, accepts parametes as FormData.
     * @param name The name of the assignment.
     * @param type The type of assignment.
     * @param description The description of the assignment.
     * @param points The amount of points the assignment is worth.
     * @param dueDate The due date of the assignment, in MM/DD/YYYY format.
     * @return The saved assignment.
     */
    @PostMapping("/create") 
    public ResponseEntity<?> createAssignment(
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam Double points,
            @RequestParam String dueDate
    ) {
        Assignment newAssignment = new Assignment(name, type, description, points, dueDate);
        Assignment savedAssignment = assignmentRepo.save(newAssignment);
        return new ResponseEntity<>(savedAssignment, HttpStatus.CREATED);
    }

    /**
     * A GET endpoint to retrieve all the assignments.
     * @return A list of all the assignments.
     */
    @Transactional
    @GetMapping("/")
    public ResponseEntity<?> getAllAssignments() {
        List<Assignment> assignments = assignmentRepo.findAll();
        List<Map<String, String>> simple = new ArrayList<>();
        for (Assignment a : assignments) {
            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(a.getId()));
            map.put("name", a.getName());
            map.put("description", a.getDescription());
            map.put("dueDate", a.getDueDate());
            map.put("points", String.valueOf(a.getPoints()));
            map.put("type", a.getType());
            simple.add(map);
        }
        return new ResponseEntity<>(simple, HttpStatus.OK);
    }

    /**
     * A POST endpoint to edit an assignment.
     * @param name The name of the assignment.
     * @param body The new information about the assignment.
     * @return The edited assignment.
     */
    @PostMapping("/edit/{name}")
    public ResponseEntity<?> editAssignment(
            @PathVariable String name,
            @RequestBody String body) {
        Assignment assignment = assignmentRepo.findByName(name);
        if (assignment != null) {
            assignment.setName(name);
            assignmentRepo.save(assignment);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Assignment not found: " + name);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/submissions/student/{studentId}")
    public ResponseEntity<?> getStudentSubmissions(@PathVariable Long studentId) {
        List<AssignmentSubmission> submissions = submissionRepo.findByStudentId(studentId);
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<Assignment> assignment = assignmentRepo.findById(id);

        if (assignment.isPresent()) {
            return ResponseEntity.ok(assignment.get().getName());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Assignment not found with ID: " + id);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * A POST endpoint to delete an assignment.
     * @param id The ID of the assignment to delete.
     * @return A JSON object indicating that the assignment was deleted.
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        Assignment assignment = assignmentRepo.findById(id).orElse(null);
        if (assignment != null) {
            assignmentRepo.delete(assignment);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Assignment deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Assignment not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * A GET endpoint used for debugging which returns information about every assignment.
     * @return Information about all the assignments.
     */
    @GetMapping("/debug")
    public ResponseEntity<?> debugAssignments() {
        List<Assignment> assignments = assignmentRepo.findAll();
        List<AssignmentDto> simple = new ArrayList<>();
        for (Assignment a : assignments) {
            simple.add(new AssignmentDto(a));
        }
        return new ResponseEntity<>(simple, HttpStatus.OK);
    }
    
    /**
     * A POST endpoint to submit an assignment.
     * @param assignmentId The ID of the assignment being submitted.
     * @param studentId The ID of the student submitting the assignment.
     * @param content The content of the student's submission.
     * @return The saved submission, if it successfully submitted.
     */
    @PostMapping("/submit/{assignmentId}")
    public ResponseEntity<?> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam Long studentId,
            @RequestParam String content,
            @RequestParam String comment,
            @RequestParam Boolean isLate
            ) {
        Assignment assignment = assignmentRepo.findById(assignmentId).orElse(null);
        Person student = personRepo.findById(studentId).orElse(null);
        if (assignment != null) {
            AssignmentSubmission submission = new AssignmentSubmission(assignment, List.of(student), content, comment, isLate);
            AssignmentSubmission savedSubmission = submissionRepo.save(submission);
            return new ResponseEntity<>(savedSubmission, HttpStatus.CREATED);
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Assignment not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * A GET endpoint to retrieve all submissions for the assignment.
     * @param assignmentId The ID of the assignment.
     * @return All submissions for the assignment.
     */
    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<?> getSubmissions(@PathVariable Long assignmentId) {
        List<AssignmentSubmission> submissions = submissionRepo.findByAssignmentId(assignmentId);
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    /**
     * A GET endpoint to retrieve the queue for an assignment.
     * @param id The ID of the assignment.
     * @return Queue for assignment, formatted in JSON
     */
    @GetMapping("/getQueue/{id}")
    public ResponseEntity<AssignmentQueue> getQueue(@PathVariable long id) {
        Optional<Assignment> optional = assignmentRepo.findById(id);
        if (optional.isPresent()) {
            Assignment assignment = optional.get();
            
            return new ResponseEntity<>(assignment.getAssignmentQueue(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getPresentationLength/{id}")
    public ResponseEntity<Long> getPresentationLength(@PathVariable long id) {
        Optional<Assignment> optional = assignmentRepo.findById(id);
        if (optional.isPresent()) {
            Assignment assignment = optional.get();
            
            return new ResponseEntity<>(assignment.getPresentationLength(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * A PUT endpoint to initialize an empty queue for an assignment.
     * @param id The ID of the assignment.
     * @return Queue for assignment, formatted in JSON
     */
    @PutMapping("/initQueue/{id}")
    public ResponseEntity<Assignment> initQueue(@PathVariable long id, @RequestBody List<List<String>> people) {
        Optional<Assignment> optional = assignmentRepo.findById(id);
        if (optional.isPresent()) {
            Assignment assignment = optional.get();
            assignment.initQueue(people.get(0), Long.parseLong(people.get(1).get(0)));
            assignmentRepo.save(assignment);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * A PUT endpoint to add a user to the waiting list
     * @param id The ID of the assignment.
     * @param person Name of person to be added to waiting list in one element array.
     * @return Updated queue for assignment, formatted in JSON
     */
    @PutMapping("/addToWaiting/{id}")
    public ResponseEntity<Assignment> addQueue(@PathVariable long id, @RequestBody List<String> person) {
        Optional<Assignment> optional = assignmentRepo.findById(id);
        if (optional.isPresent()) {
            Assignment assignment = optional.get();
            assignment.addQueue(person.get(0));
            assignmentRepo.save(assignment);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * A PUT endpoint to return a user to the working list
     * @param id The ID of the assignment.
     * @param person Name of person to be returned to the working list in one element array.
     * @return Updated queue for assignment, formatted in JSON
     */
    @PutMapping("/removeToWorking/{id}")
    public ResponseEntity<Assignment> removeQueue(@PathVariable long id, @RequestBody List<String> person) {
        Optional<Assignment> optional = assignmentRepo.findById(id);
        if (optional.isPresent()) {
            Assignment assignment = optional.get();
            assignment.removeQueue(person.get(0));
            assignmentRepo.save(assignment);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * A PUT endpoint to move a user to the completed list
     * @param id The ID of the assignment.
     * @param person Name of person to be moved to the completed list in one element array.
     * @return Updated queue for assignment, formatted in JSON
     */
    @PutMapping("/doneToCompleted/{id}")
    public ResponseEntity<Assignment> doneQueue(@PathVariable long id, @RequestBody List<String> person) {
        Optional<Assignment> optional = assignmentRepo.findById(id);
        if (optional.isPresent()) {
            Assignment assignment = optional.get();
            assignment.doneQueue(person.get(0));
            assignmentRepo.save(assignment);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * A PUT endpoint to reset a queue to its empty form.
     * @param id The ID of the assignment.
     * @return Updated queue for assignment, formatted in JSON
     */
    @PutMapping("/resetQueue/{id}")
    public ResponseEntity<Assignment> resetQueue(@PathVariable long id) {
        Optional<Assignment> optional = assignmentRepo.findById(id);
        if (optional.isPresent()) {
            Assignment assignment = optional.get();
            assignment.resetQueue();
            assignmentRepo.save(assignment);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/assignGraders/{id}")
    public ResponseEntity<?> assignGradersToAssignment( @PathVariable Long id, @RequestBody List<Long> personIds ) {
        Optional<Assignment> assignmentOptional = assignmentRepo.findById(id);
        if (!assignmentOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assignment not found");
        }

        Assignment assignment = assignmentOptional.get();
        List<Person> persons = personRepo.findAllById(personIds);

        assignment.setAssignedGraders(persons);

        assignmentRepo.save(assignment);

        System.out.println("hi");
        return ResponseEntity.ok("Persons assigned successfully");
    }

    @GetMapping("/assignedGraders/{id}")
    public ResponseEntity<?> getAssignedGraders(@PathVariable Long id) {
        Optional<Assignment> assignmentOptional = assignmentRepo.findById(id);
        if (!assignmentOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assignment not found");
        }

        Assignment assignment = assignmentOptional.get();
        List<Person> assignedGraders = assignment.getAssignedGraders();
        
        // Return just the IDs of assigned persons
        List<Long> assignedGraderIds = assignedGraders.stream()
            .map(Person::getId)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(assignedGraderIds);
    }
    
    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedAssignments(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String uid = userDetails.getUsername();
        Person user = personRepo.findByUid(uid);
        if (user == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You must be a logged in user to do this"
            );
        }

        List<Assignment> assignments = assignmentRepo.findByAssignedGraders(user);

        List<AssignmentDto> formattedAssignments = new ArrayList<>();
        for (Assignment a : assignments) {
            formattedAssignments.add(new AssignmentDto(a));
        }

        return ResponseEntity.ok(formattedAssignments);
    }
}