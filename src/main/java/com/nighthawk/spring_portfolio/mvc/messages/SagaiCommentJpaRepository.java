package com.nighthawk.spring_portfolio.mvc.messages;

import org.springframework.data.jpa.repository.JpaRepository;

// JPA is an object-relational mapping (ORM) to persistent data, originally relational databases (SQL). Today JPA implementations has been extended for NoSQL.
public interface SagaiCommentJpaRepository extends JpaRepository<SagaiComment, Long> {
}
