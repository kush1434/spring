package com.nighthawk.spring_portfolio.mvc.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/bank")
public class BankApiController {

    @Autowired
    private BankService bankService;

    // Request a loan for a bank account
    @PostMapping("/requestLoan")
    public ResponseEntity<String> requestLoan(@RequestBody LoanRequest request) {
        try {
            Bank bank = bankService.requestLoan(request.getPersonId(), request.getLoanAmount());
            return ResponseEntity.ok("Loan of amount " + request.getLoanAmount() + " granted to user with Person ID: " + request.getPersonId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Loan request failed: " + e.getMessage());
        }
    }

    @GetMapping("/profitByCategory")
    public ResponseEntity<?> getProfitByCategory(
        @RequestParam String uid, 
        @RequestParam String category
    ) {
        try {
            Bank bank = bankService.findByUid(uid);
            if (bank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Bank account not found for user: " + uid);
            }

            List<Double> profits = bank.getProfitByCategory(category);
            
            if (profits.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("No profits found for category: " + category);
            }

            return ResponseEntity.ok(profits);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving profits: " + e.getMessage());
        }
    }

    // Get the loan amount for a bank account
    @GetMapping("/loanAmount")
    public ResponseEntity<Double> getLoanAmount(@RequestParam Long personId) {
        try {
            Bank bank = bankService.findByPersonId(personId);
            return ResponseEntity.ok(bank.getLoanAmount());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}

// Request objects
@Data
@NoArgsConstructor
@AllArgsConstructor
class LoanRequest {
    private Long personId;
    private double loanAmount;
}