package com.nighthawk.spring_portfolio.mvc.bank;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/bank")
public class BankApiController {

    @Autowired
    private BankService bankService;
    
    @Autowired
    private BankJpaRepository bankJpaRepository;

    @GetMapping("/{id}/profitmap/{category}")
    public ResponseEntity<List<List<Object>>> getProfitByCategory(@PathVariable Long id, @PathVariable String category) {
        Bank bank = bankJpaRepository.findByPersonId(id);
        
        if (bank == null) {
            return ResponseEntity.notFound().build();
        }
    
        List<List<Object>> profits = bank.getProfitByCategory(category);
        return ResponseEntity.ok(profits);
    }    
    
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

    // Get the loan amount for a bank account
    @GetMapping("/{personId}/loanAmount")
    public ResponseEntity<Double> getLoanAmount(@PathVariable Long personId) {
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