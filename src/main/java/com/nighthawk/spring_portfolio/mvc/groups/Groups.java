package com.nighthawk.spring_portfolio.mvc.groups;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "groups")
public class Groups {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Person> groupMembers = new ArrayList<>();

    public Groups() {

    }

    public Groups(List<Person> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Person> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<Person> groupMembers) {
        this.groupMembers = groupMembers;
    }


    // Method to add a person to the group
    public void addPerson(Person person) {
        if (!this.groupMembers.contains(person)) {
            this.groupMembers.add(person);
            person.setGroup(this);
        }
    }

    // Method to remove a person from the group
    public void removePerson(Person person) {
        if (this.groupMembers.contains(person)) {
            this.groupMembers.remove(person);
            person.setGroup(null);
        }
    }
}