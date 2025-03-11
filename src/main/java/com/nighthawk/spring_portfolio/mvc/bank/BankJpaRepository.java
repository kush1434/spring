package com.nighthawk.spring_portfolio.mvc.bank;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankJpaRepository extends JpaRepository<Bank, Long> {

    // Method to find a bank by its UID
    Bank findByUid(Long uid);
}
