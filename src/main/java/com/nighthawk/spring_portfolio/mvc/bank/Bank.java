package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.nighthawk.spring_portfolio.mvc.person.Person;

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

    @JdbcTypeCode(SqlTypes.JSON) // Store as JSON
    @Column(columnDefinition = "jsonb") // Use JSONB for PostgreSQL, or "json" for other databases
    private List<String> stocksOwned = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON) // Store as JSON
    @Column(columnDefinition = "jsonb")
    private List<Double> gamblingProfit = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON) // Store as JSON
    @Column(columnDefinition = "jsonb")
    private List<Double> adventureGameProfit = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON) // Store as JSON
    @Column(columnDefinition = "jsonb")
    private List<Double> stocksProfit = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "person_id", unique = true, nullable = false)
    private Person person;

    public Bank(String username, double balance, double loanAmount, List<String> stocksOwned, List<Double> gamblingProfit, List<Double> adventureGameProfit, List<Double> stocksProfit) {
        this.username = username;
        this.balance = balance;
        this.loanAmount = loanAmount;
        this.stocksOwned = stocksOwned;
        this.gamblingProfit = gamblingProfit;
        this.adventureGameProfit = adventureGameProfit;
        this.stocksProfit = stocksProfit;
    }

    public List<Double> getGamblingProfit(Long uid) {
        return gamblingProfit;
    }

    public List<Double> getAdventureGameProfit(Long uid) {
        return adventureGameProfit;
    }

    public List<Double> getStocksProfit(Long uid) {
        return stocksProfit;
    }

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
}