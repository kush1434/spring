package com.nighthawk.spring_portfolio.mvc.groups;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
This class has an instance of Java Persistence API (JPA)
-- @Autowired annotation. Allows Spring to resolve and inject collaborating beans into our bean.
-- Spring Data JPA will generate a proxy instance
-- Below are some CRUD methods that we can use with our database
*/

@Service
@Transactional
public class GroupsDetailsService {  // "implements" ties ModelRepo to Spring Security
    // Encapsulate many object into a single Bean (Person, Roles, and Scrum)
    @Autowired  // Inject PersonJpaRepository
    private GroupsJpaRepository groupsJpaRepository;

    /* Person Section */

    public List<Groups> listAll() {
        return groupsJpaRepository.findAll();
    }

    public void delete(long id) {
        groupsJpaRepository.deleteById(id);
    }
}