package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

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
    
    // Get top 10 leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        try {
            List<Bank> topBanks = bankJpaRepository.findTop10ByOrderByBalanceDesc();
            List<LeaderboardEntry> leaderboard = new ArrayList<>();
            
            for (int i = 0; i < topBanks.size(); i++) {
                Bank bank = topBanks.get(i);
                leaderboard.add(new LeaderboardEntry(
                    i + 1,
                    bank.getId(),
                    bank.getUsername() != null ? bank.getUsername() : "User " + bank.getId(),
                    bank.getBalance()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", leaderboard
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error fetching leaderboard: " + e.getMessage()
            ));
        }
    }
    
    // Search leaderboard
    @GetMapping("/leaderboard/search")
    public ResponseEntity<Map<String, Object>> searchLeaderboard(@RequestParam String query) {
        try {
            List<Bank> matchedBanks = bankJpaRepository.findByUsernameContainingIgnoreCase(query);
            List<LeaderboardEntry> leaderboard = new ArrayList<>();
            
            for (int i = 0; i < matchedBanks.size(); i++) {
                Bank bank = matchedBanks.get(i);
                leaderboard.add(new LeaderboardEntry(
                    i + 1,
                    bank.getId(),
                    bank.getUsername() != null ? bank.getUsername() : "User " + bank.getId(),
                    bank.getBalance()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", leaderboard
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error searching leaderboard: " + e.getMessage()
            ));
        }
    }

    // Get individual user analytics data
    @GetMapping("/analytics/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable Long userId) {
        try {
            Bank bank = bankJpaRepository.findById(userId).orElse(null);
            if (bank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "User not found"
                ));
            }

            // Prepare analytics data
            Map<String, Object> analyticsData = new HashMap<>();
            analyticsData.put("userId", bank.getId());
            analyticsData.put("username", bank.getUsername() != null ? bank.getUsername() : "User " + bank.getId());
            analyticsData.put("balance", bank.getBalance());
            analyticsData.put("loanAmount", bank.getLoanAmount());
            analyticsData.put("dailyInterestRate", bank.getDailyInterestRate());
            analyticsData.put("riskCategory", bank.getRiskCategory());
            analyticsData.put("riskCategoryString", bank.getRiskCategoryString());
            analyticsData.put("profitMap", bank.getProfitMap());
            analyticsData.put("featureImportance", bank.getFeatureImportance());
            analyticsData.put("featureExplanations", bank.getFeatureImportanceExplanations());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", analyticsData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error fetching user analytics: " + e.getMessage()
            ));
        }
    }

    // Get user analytics by person ID (alternative endpoint)
    @GetMapping("/analytics/person/{personId}")
    public ResponseEntity<Map<String, Object>> getUserAnalyticsByPersonId(@PathVariable Long personId) {
        try {
            Bank bank = bankJpaRepository.findByPersonId(personId);
            if (bank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "User not found"
                ));
            }

            // Prepare analytics data
            Map<String, Object> analyticsData = new HashMap<>();
            analyticsData.put("userId", bank.getId());
            analyticsData.put("personId", bank.getPerson().getId());
            analyticsData.put("username", bank.getUsername() != null ? bank.getUsername() : "User " + bank.getId());
            analyticsData.put("balance", bank.getBalance());
            analyticsData.put("loanAmount", bank.getLoanAmount());
            analyticsData.put("dailyInterestRate", bank.getDailyInterestRate());
            analyticsData.put("riskCategory", bank.getRiskCategory());
            analyticsData.put("riskCategoryString", bank.getRiskCategoryString());
            analyticsData.put("profitMap", bank.getProfitMap());
            analyticsData.put("featureImportance", bank.getFeatureImportance());
            analyticsData.put("featureExplanations", bank.getFeatureImportanceExplanations());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", analyticsData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Error fetching user analytics: " + e.getMessage()
            ));
        }
    }

    // Existing endpoints remain unchanged below this point
    @GetMapping("/{id}/profitmap/{category}")
    public ResponseEntity<List<List<Object>>> getProfitByCategory(@PathVariable Long id, @PathVariable String category) {
        Bank bank = bankJpaRepository.findByPersonId(id);
        if (bank == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bank.getProfitByCategory(category));
    }  

    @GetMapping("/{id}/interestRate")
    public ResponseEntity<Double> getInterestRate(@PathVariable Long id) {
        Bank bank = bankJpaRepository.findByPersonId(id);
        if (bank == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bank.getDailyInterestRate());
    }     
    
    @PostMapping("/requestLoan")
    public ResponseEntity<String> requestLoan(@RequestBody LoanRequest request) {
        try {
            Bank bank = bankService.requestLoan(request.getPersonId(), request.getLoanAmount());
            return ResponseEntity.ok("Loan of amount " + request.getLoanAmount() + " granted to user with Person ID: " + request.getPersonId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Loan request failed: " + e.getMessage());
        }
    }
    
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

    @GetMapping("/{personId}/loanAmount")
    public ResponseEntity<Double> getLoanAmount(@PathVariable Long personId) {
        try {
            Bank bank = bankService.findByPersonId(personId);
            return ResponseEntity.ok(bank.getLoanAmount());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    @Scheduled(fixedRate = 86400000)
    public void scheduledInterestApplication() {
        applyInterestToAllLoans();
    }
    
    @PostMapping("/newLoanAmountInterest")
    public String applyInterestToAllLoans() {
        List<Bank> allBanks = bankJpaRepository.findAll();
        for (Bank bank : allBanks) {
            bank.setLoanAmount(bank.getLoanAmount() * 1.05);
        }
        bankJpaRepository.saveAll(allBanks);
        return "Applied 5% interest to all loan amounts.";
    }
}

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

@Data
@NoArgsConstructor
@AllArgsConstructor
class LeaderboardEntry {
    private int rank;
    private Long userId;
    private String username;
    private double balance;
}