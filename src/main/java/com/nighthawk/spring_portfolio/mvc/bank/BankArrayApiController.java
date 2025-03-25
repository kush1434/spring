package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.userStocks.UserStocksRepository;
import com.nighthawk.spring_portfolio.mvc.userStocks.userStocksTable;

@RestController
@RequestMapping("/api/bank")
public class BankArrayApiController extends BankArray {

    // BankRequest class moved into the ApiController
    public static class BankRequest {
        private String uid;
        private double amount;
        private String source; // "casino", "stocks", "crypto"

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        @Override
        public String toString() {
            return "{Source: " + source + ", Amount: $" + amount + "}";
        }
    }

    @Autowired
    private PersonJpaRepository personRepository;

    @Autowired
    private UserStocksRepository userStocksRepo;

    public ResponseEntity<String> updateProfit(@RequestBody BankRequest request) {
        Person user = personRepository.findByUid(request.getUid());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        userStocksTable userStocks = user.getUser_stocks();
        if (userStocks == null) {
            return new ResponseEntity<>("User stocks not found", HttpStatus.NOT_FOUND);
        }

        // Store request in profitMap
        profitMap.putIfAbsent(request.getUid(), new ArrayList<>());
        profitMap.get(request.getUid()).add(request);

        // Append profit history for the user
        String profitHistory = userStocks.getCryptoHistory() + request.getSource() + " profit: $" + request.getAmount() + "\n";
        userStocks.setCryptoHistory(profitHistory);
        userStocksRepo.save(userStocks);

        // Print logs
        System.out.println("Updated profitMap for " + request.getUid() + ": " + profitMap.get(request.getUid()));

        return new ResponseEntity<>("Profit updated successfully", HttpStatus.OK);
    }

    // Get all profit history for a user
    @PostMapping("/getProfit")
    public ResponseEntity<?> getProfit(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        if (!profitMap.containsKey(uid)) {
            return new ResponseEntity<>("No profit data found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(profitMap.get(uid), HttpStatus.OK);
    }
}