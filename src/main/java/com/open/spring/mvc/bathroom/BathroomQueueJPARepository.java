package com.open.spring.mvc.bathroom;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface BathroomQueueJPARepository extends JpaRepository<BathroomQueue, Long> {
    Optional<BathroomQueue> findByTeacherEmail(String teacherEmail);
}
