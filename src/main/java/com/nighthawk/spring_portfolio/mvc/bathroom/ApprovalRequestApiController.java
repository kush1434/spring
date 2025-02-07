package com.nighthawk.spring_portfolio.mvc.bathroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@RestController
@RequestMapping("/api/approval")
public class ApprovalRequestApiController {

    @Autowired
    private ApprovalRequestJPARepository approvalRepository;

    @PostMapping("/sendApprovalRequest")
    public ResponseEntity<Object> sendApprovalRequest(@RequestBody ApprovalRequest requestDto) {
        System.out.println("Request received: " + requestDto.getStudentName());
    
        ApprovalRequest newRequest = new ApprovalRequest(requestDto.getTeacherEmail(), requestDto.getStudentName());
        approvalRepository.save(newRequest);
    
        return new ResponseEntity<>("Approval request sent successfully!", HttpStatus.CREATED);
    }

    @GetMapping("/pendingRequests")
    public ResponseEntity<List<ApprovalRequest>> getAllPendingRequests() {
        List<ApprovalRequest> pendingRequests = approvalRepository.findAll();
        return new ResponseEntity<>(pendingRequests, HttpStatus.OK);
    }

    // ✅ Approve Request (Removes the request from the database)
    @DeleteMapping("/approveRequest")
    public ResponseEntity<Object> approveRequest(@RequestBody ApprovalRequest requestDto) {
        Optional<ApprovalRequest> request = approvalRepository.findByTeacherEmailAndStudentName(
                requestDto.getTeacherEmail(), requestDto.getStudentName());

        if (request.isPresent()) {
            approvalRepository.delete(request.get());
            return new ResponseEntity<>("Request approved", HttpStatus.OK);
        }

        return new ResponseEntity<>("Request not found", HttpStatus.NOT_FOUND);
    }

    // ✅ Deny Request (Same logic as Approve, just deleting request)
    @DeleteMapping("/denyRequest")
    public ResponseEntity<Object> denyRequest(@RequestBody ApprovalRequest requestDto) {
        Optional<ApprovalRequest> request = approvalRepository.findByTeacherEmailAndStudentName(
                requestDto.getTeacherEmail(), requestDto.getStudentName());

        if (request.isPresent()) {
            approvalRepository.delete(request.get());
            return new ResponseEntity<>("Request denied", HttpStatus.OK);
        }

        return new ResponseEntity<>("Request not found", HttpStatus.NOT_FOUND);
    }
}
