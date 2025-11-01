package com.open.spring.mvc.geminiChatbotTest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeminiChatTestRepository extends JpaRepository<GeminiChatTest, Long> {
    // This interface is intentionally left blank. 
    // Default JPA methods are used for database operations.
}
