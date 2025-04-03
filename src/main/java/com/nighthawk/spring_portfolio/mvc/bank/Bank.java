package com.nighthawk.spring_portfolio.mvc.bank;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.Column;
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, List<List<Object>>> profitMap = new HashMap<>();

    public Bank(Person person, double loanAmount) {
        this.person = person;
        this.username = person.getName();
        this.balance = person.getBalanceDouble();
        this.loanAmount = loanAmount;

        this.profitMap = new HashMap<>();
    }
    public void updateProfitMap(String category, String time, double profit) {
        if (this.profitMap == null) {
            this.profitMap = new HashMap<>();
        }

        List<Object> transaction = Arrays.asList(time, profit);

        this.profitMap.computeIfAbsent(category, k -> new ArrayList<>()).add(transaction);
    }

    public List<List<Object>> getProfitByCategory(String category) {
        return this.profitMap.getOrDefault(category, new ArrayList<>());
    }


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