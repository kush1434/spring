package com.nighthawk.spring_portfolio.mvc.backups;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighthawk.spring_portfolio.mvc.assignments.*;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.person.PersonRole;
import com.nighthawk.spring_portfolio.mvc.person.PersonRoleJpaRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@RestController
@RequestMapping("/api/imports")
public class ImportsController {

    @Autowired
    private PersonJpaRepository personRepo;
    
    @Autowired
    private AssignmentJpaRepository assignmentRepo;
    
    @Autowired
    private AssignmentSubmissionJPA submissionRepo;

    @Autowired
    private PersonRoleJpaRepository roleRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/persons")
    public ResponseEntity<String> importPersons(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length < 11) continue;
                
                try {
                    Person person = new Person();
                    person.setId(Long.parseLong(data[0]));
                    person.setBalance(data[1]);
                    person.setDob(parseDate(data[2]));
                    person.setEmail(data[3]);
                    person.setKasmServerNeeded(Boolean.parseBoolean(data[4]));
                    person.setName(data[5]);
                    person.setPassword(data[6]);
                    person.setPfp(data[7]);
                    person.setSid(data[8]);
                    person.setGrades(new ArrayList<>());
                    person.setSubmissions(new ArrayList<>());
                    
                    if (!data[9].isEmpty()) {
                        Map<String, Map<String, Object>> statsMap = objectMapper.readValue(data[9], 
                            new TypeReference<Map<String, Map<String, Object>>>() {});
                        person.setStats(statsMap);
                    }
                    person.setUid(data[10]);
                    personRepo.save(person);
                } catch (Exception e) {
                    System.err.println("Skipping invalid row: " + Arrays.toString(data) + " | Error: " + e.getMessage());
                }
            }
            return ResponseEntity.ok("Persons imported successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to import persons: " + e.getMessage());
        }
    }

    @PostMapping("/assignments")
    public ResponseEntity<String> importAssignments(@RequestParam("file") MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] fields;
            boolean isHeader = true;
            while ((fields = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (fields.length < 9) continue;

                try {
                    Long id = Long.parseLong(fields[0]);
                    String assignmentQueueJson = fields[1];
                    String description = fields[2];
                    String dueDate = fields[3];
                    String name = fields[4];
                    Double points = fields[5].isEmpty() ? 0.0 : Double.parseDouble(fields[5]);
                    Long presentationLength = fields[6].isEmpty() ? null : Long.parseLong(fields[6]);
                    String timestamp = fields[7];
                    String type = fields[8];

                    Assignment assignment = new Assignment(name, type, description, points, dueDate);
                    assignment.setId(id);
                    assignment.setTimestamp(timestamp);
                    assignment.setPresentationLength(presentationLength);

                    AssignmentQueue assignmentQueue = objectMapper.readValue(assignmentQueueJson, AssignmentQueue.class);
                    assignment.setAssignmentQueue(assignmentQueue);

                    // Ensure the grades collection is not replaced
                    assignment.setGrades(new ArrayList<>());
                    assignment.setSubmissions(new ArrayList<>());

                    assignmentRepo.save(assignment);
                } catch (Exception e) {
                    System.err.println("Skipping invalid row: " + Arrays.toString(fields) + " | Error: " + e.getMessage());
                }
            }
            return ResponseEntity.ok("Assignments imported successfully");
        } catch (IOException | CsvValidationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error importing assignments: " + e.getMessage());
        }
    }

    @PostMapping("/submissions")
    public ResponseEntity<String> importSubmissions(@RequestParam("file") MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length < 8) continue;
                
                try {
                    AssignmentSubmission submission = new AssignmentSubmission();
                    submission.setId(Long.parseLong(data[0]));
                    submission.setAssignment(assignmentRepo.findById(Long.parseLong(data[1])).orElse(null));
                    submission.setComment(data[2]);
                    submission.setContent(data[3]);
                    submission.setFeedback(data[4].isEmpty() ? null : data[4]);
                    submission.setGrade(data[5].isEmpty() ? null : Double.parseDouble(data[5]));
                    submission.setStudent(personRepo.findById(Long.parseLong(data[7])).orElse(null));
                    
                    submissionRepo.save(submission);
                } catch (Exception e) {
                    System.err.println("Skipping invalid row: " + Arrays.toString(data) + " | Error: " + e.getMessage());
                }
            }
            return ResponseEntity.ok("Submissions imported successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to import submissions: " + e.getMessage());
        }
    }

    @PostMapping("/roles")
    public ResponseEntity<String> importRoles(@RequestParam("file") MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            boolean isHeader = true;

            while ((line = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                // Parse CSV row
                Long personId = Long.parseLong(line[0]);
                Long roleId = Long.parseLong(line[1]);

                // Fetch Person and Role entities
                Person person = personRepo.findById(personId)
                    .orElseThrow(() -> new RuntimeException("Person not found with id: " + personId));
                PersonRole role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

                // Add role to person
                person.getRoles().add(role);
                personRepo.save(person); // Save the updated person entity
            }

            return ResponseEntity.ok("Roles imported successfully");
        } catch (IOException | CsvValidationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing roles: " + e.getMessage());
        }
    }

    @PostMapping("/roles_mapping")
    public ResponseEntity<String> importRolesMapping(@RequestParam("file") MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            boolean isHeader = true;

            while ((line = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                // Parse CSV row
                Long roleId = Long.parseLong(line[0]);
                String roleName = line[1];

                // Create or update Role entity
                PersonRole role = roleRepo.findById(roleId).orElse(new PersonRole());
                role.setId(roleId);
                role.setName(roleName);

                roleRepo.save(role); // Save the role entity
            }

            return ResponseEntity.ok("Roles mapping imported successfully");
        } catch (IOException | CsvValidationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing roles mapping: " + e.getMessage());
        }
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return new SimpleDateFormat("MM/dd/yyyy").parse(dateStr);
        } catch (ParseException e) {
            System.err.println("Invalid date format: " + dateStr);
            return null;
        }
    }
}
