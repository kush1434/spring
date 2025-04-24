package com.nighthawk.spring_portfolio.mvc.bank;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        try {
            // Get top 10 banks ordered by balance
            List<Bank> topBanks = bankJpaRepository.findTop10ByOrderByBalanceDesc();
            
            // Transform to leaderboard entries
            List<LeaderboardEntry> leaderboard = new ArrayList<>();
            for (int i = 0; i < topBanks.size(); i++) {
                Bank bank = topBanks.get(i);
                leaderboard.add(new LeaderboardEntry(
                    i + 1,  // rank
                    bank.getId(),  // Changed from getPersonId() to getId()
                    bank.getBalance()
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", leaderboard);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error fetching leaderboard data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

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
    
    // Repay a loan for a bank account
    @PostMapping("/repayLoan")
    public ResponseEntity<String> repayLoan(@RequestBody RepaymentRequest request) {
        try {
            Bank bank = bankService.repayLoan(request.getPersonId(), request.getRepaymentAmount());
            return ResponseEntity.ok("Loan repayment of amount " + request.getRepaymentAmount() + 
                    " processed for user with Person ID: " + request.getPersonId() + 
                    ". Remaining loan amount: " + bank.getLoanAmount());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Loan repayment failed: " + e.getMessage());
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
    
    // Schedule the interest application to run every 24 hours
    @Scheduled(fixedRate = 86400000) // 24 hours in milliseconds
    public void scheduledInterestApplication() {
        applyInterestToAllLoans();
    }
    
    // POST endpoint to apply interest to all loan amounts (kept for manual triggering if needed)
    @PostMapping("/newLoanAmountInterest")
    public String applyInterestToAllLoans() {
        List<Bank> allBanks = bankJpaRepository.findAll();

        for (Bank bank : allBanks) {
            double newLoanAmount = bank.getLoanAmount() * 1.05;
            bank.setLoanAmount(newLoanAmount);
        }

        bankJpaRepository.saveAll(allBanks);

        return "Applied 5% interest to all loan amounts.";
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

@Data
@NoArgsConstructor
@AllArgsConstructor
class RepaymentRequest {
    private Long personId;
    private double repaymentAmount;
}

// Leaderboard entry class
@Data
@NoArgsConstructor
@AllArgsConstructor
class LeaderboardEntry {
    private int rank;
    private Long userId;
    private double balance;
}