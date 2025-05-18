package com.nighthawk.spring_portfolio.mvc.userStocks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStocksRepository extends JpaRepository<userStocksTable, Long> {
    // Replaces: userStocksTable findByEmail(String email);
    // Now using built-in findById(Long id) from JpaRepository
}
