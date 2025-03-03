package com.nighthawk.spring_portfolio.mvc.forum.forumRankings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface forumRankRepository extends JpaRepository<forumRankings, Long> {
    forumRankings findByAuthor(String author); // Custom query method to find a forumRankings by author
}
