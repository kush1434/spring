package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Person person; // One-to-One relationship with the Person entity

    private double balance;
    private double loanAmount;
    private List<String> stocksOwned = new ArrayList<>();

    @ElementCollection
    private Map<String, List<Double>> profitMap = new HashMap<>();

    public Bank(Person person, double loanAmount) {
        this.person = person;
        this.username = person.getName();
        this.balance = person.getBalanceDouble();
        this.loanAmount = loanAmount;
        
        // Initialize profit map with default categories
        this.profitMap.put("gambling", new ArrayList<>());
        this.profitMap.put("adventureGame", new ArrayList<>());
        this.profitMap.put("stocks", new ArrayList<>());
    }

    // Getter for profit map
    public Map<String, List<Double>> getProfitMap() {
        return profitMap;
    }

    // Update profit map method
    public void updateProfitMap(Map<String, List<Double>> newProfitMap) {
        // Clear existing map
        this.profitMap.clear();
        
        // Add all entries from the new map
        if (newProfitMap != null) {
            this.profitMap.putAll(newProfitMap);
        }
    }

    // Method to get profits for a specific category
    public List<Double> getProfitByCategory(String category) {
        return profitMap.getOrDefault(category, new ArrayList<>());
    }

    // Method to add profit to a specific category
    public void addProfitToCategory(String category, Double profit) {
        if (!profitMap.containsKey(category)) {
            profitMap.put(category, new ArrayList<>());
        }
        profitMap.get(category).add(profit);
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

    public static Bank[] init(Person[] persons) {
        ArrayList<Bank> bankList = new ArrayList<>();

        for (Person person : persons) {
            bankList.add(new Bank(person, 0));
        }

        return bankList.toArray(new Bank[0]);
    }
}