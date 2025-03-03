package com.nighthawk.spring_portfolio.mvc.forum;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ForumInitializer implements CommandLineRunner {
    private final ForumRepository forumRepository;

    public ForumInitializer(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (forumRepository.count() == 0) { // Prevent duplicate inserts
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            forumRepository.save(new Forum("Hippo", "Introduction to Java", "Let's discuss the basics of Java programming!", currentDate, 0));
            forumRepository.save(new Forum("Cheetah", "Spring Boot Tips", "How do you optimize Spring Boot applications for performance?", currentDate, 0));
            forumRepository.save(new Forum("Elephant", "Database Optimization", "What are the best practices for indexing in SQL databases?", currentDate, 0));
            forumRepository.save(new Forum("Falcon", "Frontend vs Backend", "Which one do you prefer working on and why?", currentDate, 0));
            forumRepository.save(new Forum("Dolphin", "Machine Learning Trends", "What are the latest trends in AI and machine learning?", currentDate, 0));
            forumRepository.save(new Forum("Nandan Vallamkondu, Torin Wolff, Dylan Garrett", "Collegeboard Sorting Algorithms and Searching Submission", "Just submit your name and your github link to the homework. ", currentDate, 0));

        }
    }
}
