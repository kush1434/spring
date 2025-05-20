package com.nighthawk.spring_portfolio.mvc.mines;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.bank.Bank;
import com.nighthawk.spring_portfolio.mvc.bank.BankJpaRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/casino/mines")
public class MinesApiController {
    
    private static final Logger LOGGER = Logger.getLogger(MinesApiController.class.getName());
    
    @Autowired
    private PersonJpaRepository personJpaRepository;

    @Autowired
    private BankJpaRepository bankJpaRepository;

    private MinesBoard board;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinesRequest {
        private double bet;
        private String uid;
    }

    @GetMapping("/{xCoord}/{yCoord}")
    public ResponseEntity<Object> getMine(@PathVariable int xCoord, @PathVariable int yCoord) {
        try {
            LOGGER.info("Checking mine at coordinates (" + xCoord + ", " + yCoord + ")");
            
            if (board == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No active game"));
            }
            
            boolean isMine = board.checkMine(xCoord, yCoord);
            return ResponseEntity.ok(Map.of("isMine", isMine));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking mine", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/winnings")
    public ResponseEntity<Object> calculateWinnings(@RequestBody MinesRequest minesRequest) {
        try {
            LOGGER.info("Calculating winnings for user: " + minesRequest.getUid());
            
            if (board == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No active game"));
            }
            
            String uid = minesRequest.getUid();
            Person person = personJpaRepository.findByUid(uid);
            
            if (person == null) {
                LOGGER.warning("Person not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Person not found"));
            }

            Bank bank = bankJpaRepository.findByUsername(uid);
            if (bank == null) {
                LOGGER.warning("Bank account not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bank account not found"));
            }

            double betSize = minesRequest.getBet();
            double winnings = board.winnings() * betSize;
            
            // Record transaction
            String timestamp = Instant.now().toString();
            bank.updateProfitMap("casino_mines", timestamp, winnings - betSize);
            
            // Update balance
            double currentBalance = bank.getBalance();
            double updatedBalance = currentBalance + winnings;
            bank.setBalance(updatedBalance, "mines");
            bankJpaRepository.save(bank);

            Map<String, Object> response = new HashMap<>();
            response.put("updatedBalance", bank.getBalance());
            response.put("winnings", winnings);
            
            // Reset board after collecting winnings
            board = null;
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating winnings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/stakes/{stakes}")
    public ResponseEntity<Object> postStakes(@PathVariable String stakes, @RequestBody MinesRequest minesRequest) {
        try {
            LOGGER.info("Starting new mines game for user: " + minesRequest.getUid() + " with stakes: " + stakes);
            
            String uid = minesRequest.getUid();
            Person person = personJpaRepository.findByUid(uid);
            
            if (person == null) {
                LOGGER.warning("Person not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Person not found"));
            }

            Bank bank = bankJpaRepository.findByUsername(uid);
            if (bank == null) {
                LOGGER.warning("Bank account not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bank account not found"));
            }

            double betSize = minesRequest.getBet();
            
            // Validate bet amount
            if (betSize <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Bet amount must be greater than zero"));
            }
            
            // Check if user has enough balance
            double currentBalance = bank.getBalance();
            if (currentBalance < betSize) {
                LOGGER.warning("Insufficient balance for user: " + uid + ", balance: " + currentBalance + ", bet: " + betSize);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Insufficient balance",
                    "balance", currentBalance
                ));
            }

            // Deduct bet amount
            double updatedBalance = currentBalance - betSize;
            bank.setBalance(updatedBalance, "mines_bet");
            bankJpaRepository.save(bank);
            
            // Create new board
            board = new MinesBoard(stakes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("updatedBalance", bank.getBalance());
            response.put("gameStarted", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing stakes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/balance/{uid}")
    public ResponseEntity<Object> getBalance(@PathVariable String uid) {
        try {
            LOGGER.info("Getting balance for user: " + uid);
            
            Person person = personJpaRepository.findByUid(uid);
            if (person == null) {
                LOGGER.warning("Person not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Person not found"));
            }

            Bank bank = bankJpaRepository.findByUsername(uid);
            if (bank == null) {
                LOGGER.warning("Bank account not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bank account not found"));
            }
            
            return ResponseEntity.ok(Map.of("balance", bank.getBalance()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting balance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Object> handleMinesGame(@RequestBody MinesRequest minesRequest) {
        try {
            LOGGER.info("Saving mines game result for user: " + minesRequest.getUid());
            
            String uid = minesRequest.getUid();
            Person person = personJpaRepository.findByUid(uid);
            
            if (person == null) {
                LOGGER.warning("Person not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Person not found"));
            }

            Bank bank = bankJpaRepository.findByUsername(uid);
            if (bank == null) {
                LOGGER.warning("Bank account not found for uid: " + uid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bank account not found"));
            }

            double winAmount = minesRequest.getBet();
            double currentBalance = bank.getBalance();
            double updatedBalance = currentBalance + winAmount;
            
            // Update balance
            bank.setBalance(updatedBalance, "mines_save");
            bankJpaRepository.save(bank);

            // Reset board after saving
            board = null;

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("updatedBalance", bank.getBalance());
            response.put("transactionAmount", winAmount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving mines game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }
}