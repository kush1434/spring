package com.open.spring.mvc.geminiChatbot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface GeminiChatRepository extends JpaRepository<GeminiChat, Long> {
    // This interface is intentionally left blank. 
    // Default JPA methods are used for database operations.
}
