package com.nighthawk.spring_portfolio.mvc.bathroom;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;

// REST API controller for managing bathroom queues
@RestController
@RequestMapping("/api/queue") // Base URL for all endpoints in this controller
public class BathroomQueueApiController {

    @Autowired
    private BathroomQueueJPARepository repository; // Repository for database operations

    @Autowired
    private EmailService emailService; // Service for handling email-related functionality

    // DTO (Data Transfer Object) for queue operations
    @Getter
    public static class QueueDto {
        private String teacherEmail; // Teacher's email associated with the queue
        private String studentName;  // Name of the student to be added/removed/approved
        private String uri;          // URI for constructing approval links
    }

    // Endpoint to add a student to the queue
    @CrossOrigin(origins = {"*"})
    @PostMapping("/add")
    public ResponseEntity<Object> addToQueue(@RequestBody QueueDto queueDto) {
        // Check if a queue already exists for the given teacher
        Optional<BathroomQueue> existingQueue = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (existingQueue.isPresent()) {
            // Add the student to the existing queue
            existingQueue.get().addStudent(queueDto.getStudentName());
            repository.save(existingQueue.get()); // Save the updated queue to the database
        } else {
            // Create a new queue for the teacher and add the student
            BathroomQueue newQueue = new BathroomQueue(queueDto.getTeacherEmail(), queueDto.getStudentName());
            repository.save(newQueue); // Save the new queue to the database
        }
        return new ResponseEntity<>(queueDto.getStudentName() + " was added to " + queueDto.getTeacherEmail(), HttpStatus.CREATED);
    }

    
    // Endpoint to remove a student from the queue
    @CrossOrigin(origins = {"*"})
    @DeleteMapping("/remove")
    public ResponseEntity<Object> removeFromQueue(@RequestBody QueueDto queueDto) {
        Optional<BathroomQueue> queueEntry = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (queueEntry.isPresent()) {
            BathroomQueue bathroomQueue = queueEntry.get();
            try {
                // Remove the student from the queue
                bathroomQueue.removeStudent(queueDto.getStudentName());
                repository.save(bathroomQueue); // Save the updated queue
                return new ResponseEntity<>("Removed " + queueDto.getStudentName() + " from " + queueDto.getTeacherEmail() + "'s queue", HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                // Handle case where student is not in the queue
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
        // Handle case where no queue exists for the teacher
        return new ResponseEntity<>("Queue for " + queueDto.getTeacherEmail() + " not found", HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = {"*"})
    @DeleteMapping("/removefront/{teacher}")
    public void removeFront(@PathVariable String teacher)
    {
        Optional<BathroomQueue> queueEntry = repository.findByTeacherEmail(teacher);
        BathroomQueue bathroomQueue = queueEntry.get();
        String firstStudent = bathroomQueue.getFrontStudent();
        bathroomQueue.removeStudent(firstStudent);
        repository.save(bathroomQueue);
    }



    // Endpoint to approve the first student in the queue
    @CrossOrigin(origins = {"*"})
    @PostMapping("/approve")
    public ResponseEntity<Object> approveStudent(@RequestBody QueueDto queueDto) {
        Optional<BathroomQueue> queueEntry = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (queueEntry.isPresent()) {
            BathroomQueue bathroomQueue = queueEntry.get();
            String frontStudent = bathroomQueue.getFrontStudent();
            if (frontStudent != null && frontStudent.equals(queueDto.getStudentName())) {
                // Approve the student at the front of the queue
                bathroomQueue.approveStudent();
                repository.save(bathroomQueue);
                return new ResponseEntity<>("Approved " + queueDto.getStudentName(), HttpStatus.OK);
            } else {
                // Handle case where the student is not at the front
                return new ResponseEntity<>("Student is not at the front of the queue", HttpStatus.BAD_REQUEST);
            }
        }
        // Handle case where no queue exists for the teacher
        return new ResponseEntity<>("Queue for " + queueDto.getTeacherEmail() + " not found", HttpStatus.NOT_FOUND);
    }

    // Endpoint to send an approval email to the teacher
    @PostMapping("/sendApprovalEmail")
    public ResponseEntity<Object> sendApprovalEmail(@RequestBody QueueDto queueDto) {
        Optional<BathroomQueue> queueEntry = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (queueEntry.isPresent()) {
            BathroomQueue bathroomQueue = queueEntry.get();
            String frontStudent = bathroomQueue.getFrontStudent();
            if (frontStudent != null && frontStudent.equals(queueDto.getStudentName())) {
                try {
                    // Create a link for approving the student
                    String encodedTeacherEmail = URLEncoder.encode(queueDto.getTeacherEmail(), StandardCharsets.UTF_8.toString());
                    String encodedStudentName = URLEncoder.encode(queueDto.getStudentName(), StandardCharsets.UTF_8.toString());
                    String approvalLink = queueDto.getUri() + "/api/queue/approveLink?teacherEmail=" + encodedTeacherEmail + "&studentName=" + encodedStudentName;

                    // Construct the email details
                    EmailDetails emailDetails = new EmailDetails(
                        "dnhsbathroom@gmail.com", // Email Address
                        "Student Approval Request\n\nPlease click the link below to approve " + queueDto.getStudentName() + ":\n" + approvalLink, //Message
                        "Approval Request for Bathroom Access", //Subject
                        null
                    );

                    // Send the email
                    String emailStatus = emailService.sendSimpleMail(emailDetails);
                    if (emailStatus.equals("Email sent successfully!")) {
                        return new ResponseEntity<>("Approval email sent successfully!", HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Failed to send approval email: " + emailStatus, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } catch (UnsupportedEncodingException e) {
                    // Handle URL encoding errors
                    return new ResponseEntity<>("Error encoding URL parameters", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                // Handle case where student is not at the front
                return new ResponseEntity<>("Student is not at the front of the queue", HttpStatus.BAD_REQUEST);
            }
        }
        // Handle case where no queue exists for the teacher
        return new ResponseEntity<>("Queue for " + queueDto.getTeacherEmail() + " not found", HttpStatus.NOT_FOUND);
    }

    // Endpoint for approving a student via a link
    @GetMapping("/approveLink")
    public ResponseEntity<Object> approveStudentViaLink(@RequestParam String teacherEmail, @RequestParam String studentName) {
        Optional<BathroomQueue> queueEntry = repository.findByTeacherEmail(teacherEmail);
        if (queueEntry.isPresent()) {
            BathroomQueue bathroomQueue = queueEntry.get();
            String frontStudent = bathroomQueue.getFrontStudent();
            if (frontStudent != null && frontStudent.equals(studentName)) {
                // Approve the student
                bathroomQueue.approveStudent();
                repository.save(bathroomQueue);
                return new ResponseEntity<>("Approved " + studentName, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Student is not at the front of the queue", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Queue for " + teacherEmail + " not found", HttpStatus.NOT_FOUND);
    }

    // Endpoint to retrieve all queues
    @CrossOrigin(origins = {"*"})
    @GetMapping("/all")
    public ResponseEntity<List<BathroomQueue>> getAllQueues() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    // Endpoint to retrieve active queues
    @CrossOrigin(origins = {"*"})
    @GetMapping("/getActive")
    public ResponseEntity<Object> getActiveQueues() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }
}
