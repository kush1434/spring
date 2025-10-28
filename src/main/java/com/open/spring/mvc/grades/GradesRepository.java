package com.open.spring.mvc.grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GradesRepository extends JpaRepository<Grades, Long> {
    List<Grades> findByUidOrderByCreatedAtDesc(String uid);
    Grades findTopByUidOrderByCreatedAtDesc(String uid);
}
