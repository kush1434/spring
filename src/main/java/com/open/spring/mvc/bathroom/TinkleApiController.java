package com.open.spring.mvc.bathroom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/tinkle")
public class TinkleApiController {

    // Inject Tinkle JPA repository for database operations
    @Autowired
    private TinkleJPARepository repository;

    // Inject Person JPA repository to fetch person-related data
    @Autowired
    private PersonJpaRepository personRepository;

    // Data Transfer Object for Tinkle-related input/output
    @Getter
    @Setter
    public static class TinkleDto {
        private String studentEmail;  // Used to identify the person
        private String timeIn;        // String of time-in/time-out pairs
    }

    /**
     * Add or update time-in data for a student by email.
     */
    @PostMapping("/add")
    public ResponseEntity<Object> timeInOut(@RequestBody TinkleDto tinkleDto) {
        Optional<Tinkle> student = repository.findByPersonName(tinkleDto.getStudentEmail());

        if (student.isPresent()) {
            student.get().addTimeIn(tinkleDto.getTimeIn());
            repository.save(student.get());
            return new ResponseEntity<>(student.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Return a list of all Tinkle entries in the database.
     */
    @GetMapping("/all")
    public List<Tinkle> getAll() {
        return repository.findAll();
    }

    /**
     * Fetch a specific student's bathroom stats by name.
     */
    @GetMapping("/{name}")
    public ResponseEntity<Object> getTinkle(@PathVariable String name) {
        Optional<Tinkle> tinkle = repository.findByPersonName(name);

        if (tinkle.isPresent()) {
            return new ResponseEntity<>(tinkle.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Populate initial Tinkle records for all people if they don't already exist.
     */
    @GetMapping("/repopulate")
    public ResponseEntity<Object> populatePeople() {
        var personArray = personRepository.findAllByOrderByNameAsc();

        for (Person person : personArray) {
            Tinkle tinkle = new Tinkle(person, "");
            Optional<Tinkle> tinkleFound = repository.findByPersonName(tinkle.getPersonName());
            if (tinkleFound.isEmpty()) {
                repository.save(tinkle);
            }
        }

        return ResponseEntity.ok("Complete");
    }

    /**
     * Retrieve a student's `timeIn` string directly from in-memory cache.
     * This seems to depend on another controller (ApprovalRequestApiController).
     */
    @GetMapping("/timeIn/{studentName}")
    public ResponseEntity<Object> getTimeIn(@PathVariable String studentName) {
        System.out.println("🔍 Fetching timeIn for: " + studentName);

        String timeIn = ApprovalRequestApiController.getTimeInFromMemory(studentName);

        if (timeIn != null) {
            System.out.println("Retrieved timeIn from memory for " + studentName + ": " + timeIn);
            return ResponseEntity.ok(timeIn);
        } else {
            System.out.println("Student not found in memory: " + studentName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }
    }

    /**
     * Clears all bathroom records from the database.
     * Requires the requester to be an admin (checked via request attribute).
     */
    @DeleteMapping("/bulk/clear")
    public ResponseEntity<?> clearTable(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (role == null || !role.equals("ADMIN")) {
            return new ResponseEntity<>("Unauthorized - Admin access required", HttpStatus.UNAUTHORIZED);
        }

        try {
            repository.deleteAll();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All bathroom records have been cleared");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to clear table: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extracts all `Tinkle` entries and returns them as simplified `TinkleDto` objects.
     */
    @GetMapping("/bulk/extract")
    public ResponseEntity<List<TinkleDto>> bulkExtract() {
        List<Tinkle> tinkleList = repository.findAll();

        List<TinkleDto> tinkleDtos = new ArrayList<>();
        for (Tinkle tinkle : tinkleList) {
            TinkleDto dto = new TinkleDto();
            dto.setStudentEmail(tinkle.getPersonName());
            dto.setTimeIn(tinkle.getTimeIn());
            tinkleDtos.add(dto);
        }

        return new ResponseEntity<>(tinkleDtos, HttpStatus.OK);
    }

    /**
     * Bulk creation or update of Tinkle entries.
     * Updates existing entries if found, otherwise creates new ones.
     */
    @PostMapping("/bulk/create")
    public ResponseEntity<Object> bulkCreateTinkles(@RequestBody List<TinkleDto> tinkleDtos) {
        List<String> createdTinkles = new ArrayList<>();
        List<String> duplicateTinkles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (TinkleDto tinkleDto : tinkleDtos) {
            try {
                Optional<Tinkle> existingTinkle = repository.findByPersonName(tinkleDto.getStudentEmail());

                if (existingTinkle.isPresent()) {
                    // Update existing record
                    Tinkle tinkle = existingTinkle.get();
                    tinkle.addTimeIn(tinkleDto.getTimeIn());
                    repository.save(tinkle);
                    createdTinkles.add(tinkleDto.getStudentEmail() + " (updated)");
                } else {
                    // Create new record
                    Person person = personRepository.findByName(tinkleDto.getStudentEmail());

                    if (person != null) {
                        Tinkle newTinkle = new Tinkle(person, tinkleDto.getTimeIn());
                        repository.save(newTinkle);
                        createdTinkles.add(tinkleDto.getStudentEmail());
                    } else {
                        errors.add("Person not found with name: " + tinkleDto.getStudentEmail());
                    }
                }
            } catch (Exception e) {
                errors.add("Exception occurred for student: " + tinkleDto.getStudentEmail() + " - " + e.getMessage());
            }
        }

        // Create a summary response
        Map<String, Object> response = new HashMap<>();
        response.put("created", createdTinkles);
        response.put("duplicates", duplicateTinkles); // This list is currently unused
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}