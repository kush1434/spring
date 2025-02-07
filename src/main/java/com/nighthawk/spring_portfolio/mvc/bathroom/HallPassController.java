package com.nighthawk.spring_portfolio.mvc.bathroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;

@RestController
@RequestMapping("/api/hallpass")
public class HallPassController {

    @Autowired private HallPassService hallPassService;
    @Getter
    public static class HallPassRequestDTO
    {
        private String userName;
        private Long teacherId;
        private int period;
        private String activity;
        
    }
    /**
     * Endpoint to request a hall pass by providing the user's email address.
     */
    //@CrossOrigin(origins = "http://127.0.0.1:4100")
    @PostMapping("/request")
    public ResponseEntity<Object> requestHallPass(@RequestBody HallPassRequestDTO request) {
        try {
            HallPass pass = hallPassService.requestPass(    
                request.getTeacherId(),
                request.getPeriod(),
                request.getActivity(),
                request.getUserName()
            );
            return ResponseEntity.ok(pass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint to check out (return) a hall pass.
     */
    //@CrossOrigin(origins = "http://127.0.0.1:4100")
    @PostMapping("/checkout")
    public ResponseEntity<Object> checkoutHallPass(@RequestParam("email") String emailAddress) {
        try {
            boolean pass = hallPassService.checkoutPass(emailAddress);
            return ResponseEntity.ok(pass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    @CrossOrigin(origins = "http://127.0.0.1:8080")
    @GetMapping("/getTeacher")
    public ResponseEntity<Object> getTeacher(@RequestParam("fname") String firstName, 
    @RequestParam("lname") String lastName) {
        try {
            Teacher teacher = hallPassService.getTeacherByName(firstName, lastName);
            return ResponseEntity.ok(teacher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    //@CrossOrigin(origins = "http://127.0.0.1:4100")
    @GetMapping("/getactivepass")
    public ResponseEntity<Object> getPass(@RequestParam("email") String emailAddress) {
        try {
            HallPass hallpass = hallPassService.getActivePassForUser(emailAddress);
            return ResponseEntity.ok(hallpass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
