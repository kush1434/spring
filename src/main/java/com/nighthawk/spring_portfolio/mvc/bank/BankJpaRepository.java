package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankJpaRepository extends JpaRepository<Bank, Long> {
    // Find bank by person_id
    Bank findByPersonId(Long personId);
    Bank findByUsername(String username);
    
    // Find top 10 banks ordered by balance in descending order (for leaderboard)
    List<Bank> findTop10ByOrderByBalanceDesc();
}