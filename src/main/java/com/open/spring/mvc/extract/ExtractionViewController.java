package com.open.spring.mvc.extract;

import java.util.*;

import lombok.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Convert;

import com.open.spring.mvc.groups.GroupsJpaRepository;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;
import com.vladmihalcea.hibernate.type.json.JsonType;

import com.open.spring.mvc.groups.Groups;


@Controller
@RequestMapping("mvc/extract")
public class ExtractionViewController {
    /////////////////////////////////////////
    /// Autowired Jpa Repositories

    @Autowired
    private PersonJpaRepository personJpaRepository;

    @Autowired
    private GroupsJpaRepository groupsJpaRepository;

    /////////////////////////////////////////
    /// Export Objects

    // person class based on person table schema (no relationships)
    @Data
    @AllArgsConstructor
    @Convert(attributeName = "person", converter = JsonType.class)
    public class PersonEmpty {
        private Long id;
        private String uid;
        private String password;
        private String email;
        private String name;
        private String pfp;
        private String sid;
        private Boolean kasmServerNeeded;
        private Map<String, Map<String, Object>> stats;
    }

    // group class based on group table schema (no relationships)
    @Data
    @AllArgsConstructor
    @Convert(attributeName = "group", converter = JsonType.class)
    public class GroupEmpty {
        private Long id;
        private String name;
        private String period;
    }

    // group members class based on group table schema (no relationships)
    @Data
    @AllArgsConstructor
    @Convert(attributeName = "group_members", converter = JsonType.class)
    public class GroupMemberEmpty {
        private Long person_id;
        private Long group_id;
    }

    /////////////////////////////////////////
    /// Single Extracts

    @GetMapping("/person/{id}")
    public ResponseEntity<PersonEmpty> extractPersonById(@PathVariable("id") long id) {
        if (!personJpaRepository.existsById(id)) {
            return new ResponseEntity<PersonEmpty>(HttpStatus.NOT_FOUND);
        }
        Person person = personJpaRepository.findById(id).get();
        //build a PersonEmpty based on the person
        PersonEmpty personEmpty = new PersonEmpty(
                person.getId(),
                person.getUid(),
                person.getPassword(),
                person.getEmail(),
                person.getName(),
                person.getPfp(),
                person.getSid(),
                person.getKasmServerNeeded(),
                person.getStats());
                
        return new ResponseEntity<PersonEmpty>(personEmpty, HttpStatus.OK);
    }

    @GetMapping("/group/{id}")
    public ResponseEntity<GroupEmpty> extractGroupById(@PathVariable("id") long id) {
        if (!groupsJpaRepository.findById(id).isPresent()) {
            return new ResponseEntity<GroupEmpty>(HttpStatus.NOT_FOUND);
        }
        Groups group = groupsJpaRepository.findById(id).get();
        GroupEmpty groupEmpty = new GroupEmpty(group.getId(), group.getName(), group.getPeriod());
        return new ResponseEntity<GroupEmpty>(groupEmpty, HttpStatus.OK);
    }

    @GetMapping("/group/{id}/members")
    @Transactional(readOnly = true)
    public ResponseEntity<List<GroupMemberEmpty>> extractGroupMembersById(@PathVariable("id") long id) {
        if (!groupsJpaRepository.findById(id).isPresent()) {
            return new ResponseEntity<List<GroupMemberEmpty>>(HttpStatus.NOT_FOUND);
        }
        Groups group = groupsJpaRepository.findById(id).get();
        ArrayList<GroupMemberEmpty> groupMemberEmpties = new ArrayList<GroupMemberEmpty>(0);
        group.getGroupMembers().stream().forEach(groupMember -> {
            groupMemberEmpties.add(new GroupMemberEmpty(groupMember.getId(), id));
        });
        return new ResponseEntity<List<GroupMemberEmpty>>(groupMemberEmpties, HttpStatus.OK);
    }

    /////////////////////////////////////////
    /// Multi Extracts (all)

    @GetMapping("all/person")
    public ResponseEntity<List<PersonEmpty>> extractAllPerson() {
        List<Person> personList = personJpaRepository.findAll();
        ArrayList<PersonEmpty> personEmpties = new ArrayList<PersonEmpty>(0);
        personList.stream().forEach(person -> {
            personEmpties.add(new PersonEmpty(
                    person.getId(),
                    person.getUid(),
                    person.getPassword(),
                    person.getEmail(),
                    person.getName(),
                    person.getPfp(),
                    person.getSid(),
                    person.getKasmServerNeeded(),
                    person.getStats()));
        });
        return new ResponseEntity<List<PersonEmpty>>(personEmpties, HttpStatus.OK);
    }

    @GetMapping("all/group")
    public ResponseEntity<List<GroupEmpty>> extractAllGroups() {
        List<Groups> groupsList = groupsJpaRepository.findAll();
        ArrayList<GroupEmpty> groupEmpties = new ArrayList<GroupEmpty>(0);
        groupsList.stream().forEach(group -> {
            groupEmpties.add(new GroupEmpty(
                    group.getId(),
                    group.getName(),
                    group.getPeriod()));
        });
        return new ResponseEntity<List<GroupEmpty>>(groupEmpties, HttpStatus.OK);
    }

    /////////////////////////////////////////
    /// Multi Extracts (all sets)

    @PostMapping("all/person/fromRanges")
    public ResponseEntity<List<PersonEmpty>> extractAllPersonFromRanges(@RequestBody List<List<Long>> personIdRanges) {
        ArrayList<PersonEmpty> personEmpties = new ArrayList<PersonEmpty>(0);

        for (int i = 0; i < personIdRanges.size(); i++) {
            if (personIdRanges.get(i).size() < 2) {
                continue;
            }
            Long id0 = personIdRanges.get(i).get(0);
            Long id1 = personIdRanges.get(i).get(1);
            if (id0 > id1) {
                continue;
            }
            List<Person> personList = personJpaRepository.findAllByIdBetween(id0, id1);

            personList.stream().forEach(person -> {
                personEmpties.add(new PersonEmpty(
                        person.getId(),
                        person.getUid(),
                        person.getPassword(),
                        person.getEmail(),
                        person.getName(),
                        person.getPfp(),
                        person.getSid(),
                        person.getKasmServerNeeded(),
                        person.getStats()));
            });
        }
        return new ResponseEntity<List<PersonEmpty>>(personEmpties, HttpStatus.OK);
    }
}