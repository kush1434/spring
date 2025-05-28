package com.open.spring.mvc.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Crypto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String symbol; // Ticker symbol, e.g., BTC

    @Column(unique = true)
    private String name;   // Full name, e.g., Bitcoin

    private double price;  // Current price in USD
    private double changePercentage; // 24-hour percentage change

    // Custom constructor to match the usage in CryptoService
    public Crypto(String symbol, String name, double price, double changePercentage) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.changePercentage = changePercentage;
    }
}
