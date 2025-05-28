package com.open.spring.mvc.person;



import org.springframework.data.jpa.repository.JpaRepository;



public interface  PersonUserMappingJpaRepository extends JpaRepository<PersonUserMapping, Long> {
    PersonUserMapping findByUserId(Long User_id);
}
