package com.nighthawk.spring_portfolio.mvc.forum.forumBlogs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface forumBlogRepository extends JpaRepository<forumBlogs, Long> {
    forumBlogs findByTitle(String title);
    forumBlogs findByAuthor(String author);
    forumBlogs findById(long id);
}
