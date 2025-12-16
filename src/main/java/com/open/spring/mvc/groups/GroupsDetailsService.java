package com.open.spring.mvc.groups;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.open.spring.mvc.person.PersonJpaRepository;

/*
This class has an instance of Java Persistence API (JPA)
-- @Autowired annotation. Allows Spring to resolve and inject collaborating beans into our bean.
-- Spring Data JPA will generate a proxy instance
-- Below are some CRUD methods that we can use with our database
*/

@Service
@Transactional
public class GroupsDetailsService {  // "implements" ties ModelRepo to Spring Security
    @Autowired  // Inject GroupsJpaRepository
    private GroupsJpaRepository groupsJpaRepository;
    private PersonJpaRepository personRepository;

    /* Groups Section */

    public List<Groups> listAll() {
        return groupsJpaRepository.findAll();
    }

    public List<Groups> listAllWithMembers() {
        return groupsJpaRepository.findAllWithMembers();
    }

    public Groups get(long id) {
        Optional<Groups> groupOpt = groupsJpaRepository.findById(id);
        return groupOpt.orElse(null);
    }

    public List<Groups> findGroupsByPersonId(long personId) {
        return groupsJpaRepository.findGroupsByPersonId(personId);
    }

    public List<Groups> findGroupsByPersonIdWithMembers(long personId) {
        return groupsJpaRepository.findGroupsByPersonIdWithMembers(personId);
    }

    public void delete(long id) {
        groupsJpaRepository.deleteById(id);
    }

}