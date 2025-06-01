package com.open.spring.mvc.bathroom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional; 

public interface TinkleJPARepository extends JpaRepository<Tinkle, Long> {
    // Optional<Tinkle> findByStudentEmail(String studentEmail);'
    Optional<Tinkle> findByPersonName(String personName);

    @Modifying
    @Transactional
    @Query("DELETE FROM Tinkle")
    void deleteAllRowsInBulk();
}
