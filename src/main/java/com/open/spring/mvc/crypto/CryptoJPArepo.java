package com.open.spring.mvc.crypto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptoJPArepo extends JpaRepository<Crypto, Long> {
    // Custom queries to find Crypto by symbol or name
    Crypto findBySymbol(String symbol); // To find by ticker symbol (e.g., BTC)
    Crypto findByNameIgnoreCase(String name); // To find by full name (e.g., Bitcoin)
    Crypto findBySymbolOrNameIgnoreCase(String symbol, String name);

}
