package com.open.spring.mvc.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username);
    List<User> findByUsernameIgnoreCase(String username);
    User findById(Integer playerid);
}
