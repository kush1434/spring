package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
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
    private List<Double> gamblingProfit = new ArrayList<>();
    private List<Double> adventureGameProfit = new ArrayList<>();
    private List<Double> stocksProfit = new ArrayList<>();

    public Bank(Person person, double loanAmount, List<String> stocksOwned, List<Double> gamblingProfit, List<Double> adventureGameProfit, List<Double> stocksProfit) {
        this.person = person;
        this.username = person.getName();
        this.balance = person.getBalanceDouble();
        this.loanAmount = loanAmount;
        this.stocksOwned = stocksOwned;
        this.gamblingProfit = gamblingProfit;
        this.adventureGameProfit = adventureGameProfit;
        this.stocksProfit = stocksProfit;
    }

    // Method to get gambling profits for the user
    public List<Double> getGamblingProfit(String person_id) {
        return gamblingProfit;  // Return gambling profit for the user based on UID (now a String)
    }

    // Method to get adventure game profits for the user
    public List<Double> getAdventureGameProfit(String person_id) {
        return adventureGameProfit;  // Return adventure game profit for the user based on UID (now a String)
    }

    // Method to get stocks profits for the user
    public List<Double> getStocksProfit(String person_id) {
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
    public static Bank[] init(Person[] persons) {
        ArrayList<Bank> bankList = new ArrayList<>();

        for (Person person : persons) {
            bankList.add(new Bank(person, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        }

        return bankList.toArray(new Bank[0]);
    }
}