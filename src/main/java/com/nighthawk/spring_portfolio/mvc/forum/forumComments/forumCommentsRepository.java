package com.nighthawk.spring_portfolio.mvc.forum.forumComments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface forumCommentsRepository extends JpaRepository<forumComments, Long> {
    List<forumComments> findByForumId(Long forumId); 
    List<forumComments> findAll(); // Custom method to find comments by forumId
}
