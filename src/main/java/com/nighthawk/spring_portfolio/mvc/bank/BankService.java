package com.nighthawk.spring_portfolio.mvc.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class BankService {

    @Autowired
    private BankJpaRepository bankRepository;

    // Find by UID instead of username
    public Bank findByUid(String uid) {
        return bankRepository.findByUid(uid);  // Find by UID instead of username
    }

    // Request a loan using the UID
    public Bank requestLoan(String uid, double loanAmount) {
        Bank bank = bankRepository.findByUid(uid);
        bank.requestLoan(loanAmount);  // Increase the loan amount
        return bankRepository.save(bank);
    }

    // Additional methods using UID can be added here as needed
}
