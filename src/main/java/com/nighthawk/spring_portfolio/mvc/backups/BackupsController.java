package com.nighthawk.spring_portfolio.mvc.backups;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighthawk.spring_portfolio.mvc.assignments.Assignment;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentJpaRepository;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentQueue;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentSubmissionJPA;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.person.PersonRole;
import com.nighthawk.spring_portfolio.mvc.person.PersonRoleJpaRepository;
import com.opencsv.CSVWriter;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/export")
public class BackupsController {

    @Autowired
    private PersonJpaRepository personRepo;

    @Autowired
    private AssignmentJpaRepository assignmentRepo;

    @Autowired
    private AssignmentSubmissionJPA submissionRepo;

    @Autowired
    private PersonRoleJpaRepository roleRepo;

    /**
     * Export all persons to a CSV file.
     */
    @GetMapping("/persons")
    public ResponseEntity<byte[]> exportPersons() throws IOException {
        // Fetch all persons and map them to CSV rows
        List<String> data = personRepo.findAll().stream()
                .map(p -> String.join(",", 
                    String.valueOf(p.getId()), 
                    p.getBalance(), 
                    formatTimestamp(p.getDob()), 
                    p.getEmail(), 
                    String.valueOf(p.getKasmServerNeeded()), 
                    p.getName(), 
                    p.getPassword(), 
                    p.getPfp(), 
                    p.getSid(), 
                    serializeStats(p.getStats()), // Serialize stats to JSON
                    p.getUid()
                ))
                .collect(Collectors.toList());
    
        // Define the CSV header
        String header = "id,balance,dob,email,kasm_server_needed,name,password,pfp,sid,stats,uid\n";
    
        // Combine header and data into a single CSV string
        String csvContent = header + String.join("\n", data);
    
        // Create and return the CSV file as a ResponseEntity
        return createCSVResponse("persons.csv", csvContent);
    }

    private String serializeStats(Map<String, Map<String, Object>> stats) {
        if (stats == null || stats.isEmpty()) {
            return "{}"; // Return an empty JSON object if stats is null or empty
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(stats); // Serialize stats to JSON
        } catch (JsonProcessingException e) {
            return "{}"; // Fallback to an empty JSON object if serialization fails
        }
    }

    /**
     * Export all assignments to a CSV file.
     */
    @Transactional
    @GetMapping("/assignments")
    public ResponseEntity<String> exportAssignments() {
        List<Assignment> assignments = assignmentRepo.findAll();
        ObjectMapper objectMapper = new ObjectMapper();

        StringBuilder csvData = new StringBuilder();
        // Header row
        csvData.append("id,assignment_queue,description,due_date,name,points,presentation_length,timestamp,type\n");

        for (Assignment assignment : assignments) {
            try {
                // Serialize AssignmentQueue to JSON
                String assignmentQueueJson = assignment.getAssignmentQueue() != null
                        ? objectMapper.writeValueAsString(assignment.getAssignmentQueue())
                        : "null";

                // Append each field to the CSV row, ensuring proper escaping
                csvData.append(escapeCsvField(assignment.getId().toString())).append(",")
                        .append(escapeCsvField(assignmentQueueJson)).append(",")
                        .append(escapeCsvField(assignment.getDescription())).append(",")
                        .append(escapeCsvField(assignment.getDueDate())).append(",")
                        .append(escapeCsvField(assignment.getName())).append(",")
                        .append(escapeCsvField(assignment.getPoints().toString())).append(",")
                        .append(escapeCsvField(assignment.getPresentationLength() != null ? assignment.getPresentationLength().toString() : "")).append(",")
                        .append(escapeCsvField(assignment.getTimestamp())).append(",")
                        .append(escapeCsvField(assignment.getType()))
                        .append("\n");
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>("Error generating CSV", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=assignments.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

        return new ResponseEntity<>(csvData.toString(), headers, HttpStatus.OK);
    }

    /**
     * Export all submissions to a CSV file.
     */
    @GetMapping("/submissions")
    public ResponseEntity<byte[]> exportSubmissions() throws IOException {
        List<String> data = submissionRepo.findAll().stream()
                .map(s -> String.join(",", 
                    String.valueOf(s.getId()), 
                    String.valueOf(s.getAssignment().getId()), 
                    "\"" + s.getComment() + "\"",  // Encapsulate text fields in quotes
                    "\"" + s.getContent() + "\"", 
                    "\"" + (s.getFeedback() != null ? s.getFeedback() : "") + "\"", 
                    String.valueOf(s.getGrade() != null ? s.getGrade() : ""), 
                    String.valueOf(s.getAssignment().getId()), 
                    String.valueOf(s.getStudent().getId())))
                .collect(Collectors.toList());

        String csvContent = "id,assignmentid,comment,content,feedback,grade,assignment_id,student_id\n" + 
                            String.join("\n", data);

        return createCSVResponse("submissions.csv", csvContent);
    }

    /**
     * Helper method to create a CSV file response.
     */
    private ResponseEntity<byte[]> createCSVResponse(String fileName, String content) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(content);
        writer.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }

    /**
     * Helper method to format a Date object to a timestamp string.
     */
    private String formatTimestamp(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @GetMapping("/roles")
    public void exportRoles(HttpServletResponse response) throws IOException {
        // Set response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=person_roles.csv");

        // Fetch all PersonRole records using the correct repository
        Collection<Person> personRoles = personRepo.findAll(); // Correct repository

        // Write CSV data
        try (PrintWriter writer = response.getWriter();
            CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write CSV header
            String[] header = {"person_id", "role_id"};
            csvWriter.writeNext(header);

            // Write CSV rows
            for (Person personRole : personRoles) {
                // Loop through each role for the person and create a separate row for each
                for (PersonRole role : personRole.getRoles()) {
                    String[] row = {
                        personRole.getId().toString(),
                        role.getId().toString()  // Assuming Role has getId() method
                    };
                    csvWriter.writeNext(row);
                }
            }
        }
    }

    @GetMapping("/roles_mapping")
    public ResponseEntity<byte[]> exportRoles() throws IOException {
        List<String> data = roleRepo.findAll().stream()
            .map(role -> role.getId() + "," + role.getName())
            .collect(Collectors.toList());

        String csvContent = "id,name\n" + String.join("\n", data);
        return createCSVResponse("roles.csv", csvContent);
    }




    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Escape double quotes by doubling them
        String escapedField = field.replace("\"", "\"\"");
        // Enclose the field in double quotes if it contains commas, newlines, or double quotes
        if (escapedField.contains(",") || escapedField.contains("\n") || escapedField.contains("\"")) {
            return "\"" + escapedField + "\"";
        }
        return escapedField;
    }
}