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
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.vladmihalcea.hibernate.type.json.JsonType;

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
    private Person person;

    private double balance;
    private double loanAmount;

    private Map<String, List<Double>> profitMap = new HashMap<>();

    // Updated constructor to match the new profitMap type
    public Bank(Person person, double loanAmount, 
                List<Double> initialProfits, 
                List<Double> someOtherList1, 
                List<Double> someOtherList2, 
                List<Double> someOtherList3) {
        this.person = person;
        this.username = person.getName();
        this.balance = person.getBalanceDouble();
        this.loanAmount = loanAmount;
    }

    // Method to update profitMap with a new profit entry
    public void updateProfitMap(String key, Double profit) {
        // If the key doesn't exist, create a new list
        if (!this.profitMap.containsKey(key)) {
            this.profitMap.put(key, new ArrayList<>());
        }
        
        // Add the new profit to the list for this key
        this.profitMap.get(key).add(profit);
    }

    // Method to get profits for the user
    public List<Double> getProfitMap(String key) {
        return this.profitMap.getOrDefault(key, new ArrayList<>());
    }

    // Rest of the methods remain the same
    public void requestLoan(double loanAmount) {
        this.loanAmount += loanAmount;
        this.balance += loanAmount;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

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