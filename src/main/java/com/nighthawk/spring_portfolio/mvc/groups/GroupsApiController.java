package com.nighthawk.spring_portfolio.mvc.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;

import lombok.Getter;

@RestController
@RequestMapping("/api/groups")
public class GroupsApiController {

    @Autowired
    private GroupsJpaRepository groupsRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    // DTO for creating a new group
    @Getter
    public static class GroupDto {
        private List<Long> personIds;
    }

    /**
     * Get all groups
     */
    @GetMapping
    public ResponseEntity<List<Groups>> getAllGroups() {
        return new ResponseEntity<>(groupsRepository.findAll(), HttpStatus.OK);
    }

    /**
     * Get a group by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Groups> getGroupById(@PathVariable Long id) {
        Optional<Groups> group = groupsRepository.findById(id);
        if (group.isPresent()) {
            return new ResponseEntity<>(group.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Create a new group with multiple people
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createGroup(@RequestBody GroupDto groupDto) {
        try {
            // Create a new group
            Groups group = new Groups();
            group.setGroupMembers(new ArrayList<>());
            
            // Save the group first to generate an ID
            Groups savedGroup = groupsRepository.save(group);
            
            // Find and add each person to the group
            for (Long personId : groupDto.getPersonIds()) {
                Optional<Person> optionalPerson = personRepository.findById(personId);
                if (optionalPerson.isPresent()) {
                    Person person = optionalPerson.get();
                    savedGroup.addPerson(person);
                    personRepository.save(person); // Save person with updated group reference
                }
            }
            
            // Save the group again with all members
            return new ResponseEntity<>(groupsRepository.save(savedGroup), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating group: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Add people to an existing group
     */
    @PutMapping("/{id}/addPeople")
    public ResponseEntity<Object> addPeopleToGroup(@PathVariable Long id, @RequestBody List<Long> personIds) {
        Optional<Groups> optionalGroup = groupsRepository.findById(id);
        if (optionalGroup.isPresent()) {
            Groups group = optionalGroup.get();
            
            for (Long personId : personIds) {
                Optional<Person> optionalPerson = personRepository.findById(personId);
                if (optionalPerson.isPresent()) {
                    Person person = optionalPerson.get();
                    group.addPerson(person);
                    personRepository.save(person);
                }
            }
            
            return new ResponseEntity<>(groupsRepository.save(group), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Remove people from a group
     */
    @PutMapping("/{id}/removePeople")
    public ResponseEntity<Object> removePeopleFromGroup(@PathVariable Long id, @RequestBody List<Long> personIds) {
        Optional<Groups> optionalGroup = groupsRepository.findById(id);
        if (optionalGroup.isPresent()) {
            Groups group = optionalGroup.get();
            
            for (Long personId : personIds) {
                Optional<Person> optionalPerson = personRepository.findById(personId);
                if (optionalPerson.isPresent()) {
                    Person person = optionalPerson.get();
                    group.removePerson(person);
                    personRepository.save(person);
                }
            }
            
            return new ResponseEntity<>(groupsRepository.save(group), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Update group table number
     */
    @PutMapping("/{id}/table")
    public ResponseEntity<Object> updateGroupTable(@PathVariable Long id, @RequestBody Map<String, Integer> requestBody) {
        Integer table = requestBody.get("table");
        if (table == null) {
            return new ResponseEntity<>("Table number is required", HttpStatus.BAD_REQUEST);
        }
        
        Optional<Groups> optionalGroup = groupsRepository.findById(id);
        if (optionalGroup.isPresent()) {
            Groups group = optionalGroup.get();
            return new ResponseEntity<>(groupsRepository.save(group), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Delete a group (but not its members)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteGroup(@PathVariable Long id) {
        Optional<Groups> optionalGroup = groupsRepository.findById(id);
        if (optionalGroup.isPresent()) {
            Groups group = optionalGroup.get();
            
            // Unlink all people from this group
            List<Person> members = new ArrayList<>(group.getGroupMembers());
            for (Person person : members) {
                group.removePerson(person);
                personRepository.save(person);
            }
            
            groupsRepository.deleteById(id);
            return new ResponseEntity<>("Group deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Find groups containing a specific person
     */
    @GetMapping("/person/{personId}")
    public ResponseEntity<List<Groups>> getGroupsByPersonId(@PathVariable Long personId) {
        List<Groups> groups = groupsRepository.findGroupsByPersonId(personId);
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }
}