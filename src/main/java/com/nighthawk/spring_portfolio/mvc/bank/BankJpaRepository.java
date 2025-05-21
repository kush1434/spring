package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BankJpaRepository extends JpaRepository<Bank, Long> {
    // Find bank by person_id
    Bank findByPersonId(Long personId);
    Bank findByUsername(String username);
    Bank findByUid(String uid);
    
    // Find top 10 banks ordered by balance in descending order (for leaderboard)
    List<Bank> findTop10ByOrderByBalanceDesc();
    @Query("SELECT p FROM Bank p ORDER BY CAST(p.balance AS double) DESC LIMIT 5")
    List<Bank> findTop5ByOrderByBalanceDesc();
    List<Bank> findByUsernameContainingIgnoreCase(String username);
}