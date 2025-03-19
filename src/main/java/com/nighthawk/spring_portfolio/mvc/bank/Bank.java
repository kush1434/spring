package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long uid;

    @Column(unique = true, nullable = false)
    private String username;

    private double balance;
    private double loanAmount;
    private List<String> stocksOwned = new ArrayList<>();
    private List<Double> gamblingProfit = new ArrayList<>();
    private List<Double> adventureGameProfit = new ArrayList<>();
    private List<Double> stocksProfit = new ArrayList<>();

    public Bank(String username, double balance, double loanAmount, List<String> stocksOwned, List<Double> gamblingProfit, List<Double> adventureGameProfit, List<Double> stocksProfit) {
        this.username = username;
        this.balance = balance;
        this.loanAmount = loanAmount;
        this.stocksOwned = stocksOwned;
        this.gamblingProfit = gamblingProfit;
        this.adventureGameProfit = adventureGameProfit;
        this.stocksProfit = stocksProfit;
    }

    // Method to get gambling profits for the user
    public List<Double> getGamblingProfit(Long uid) {
        return gamblingProfit;  // Return gambling profit for the user based on UID (now a String)
    }

    // Method to get adventure game profits for the user
    public List<Double> getAdventureGameProfit(Long uid) {
        return adventureGameProfit;  // Return adventure game profit for the user based on UID (now a String)
    }

    // Method to get stocks profits for the user
    public List<Double> getStocksProfit(Long uid) {
        return stocksProfit;  // Return stock profits for the user based on UID (now a String)
    }

    // Method to request a loan
    public void requestLoan(double loanAmount) {
        this.loanAmount += loanAmount;  // Increase the loan amount
        this.balance += loanAmount;  // Add the loan amount to the balance
    }

    // Method to get the current loan amount
    public double getLoanAmount() {
        return loanAmount;
    }

    // Method to calculate daily interest on the loan
    public double dailyInterestCalculation() {
        double interestRate = 0.05;  
        return loanAmount * interestRate;
    }
}