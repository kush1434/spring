package com.open.spring.mvc.groups;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.open.spring.mvc.person.PersonDetailsService;
import com.open.spring.mvc.person.Person;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/mvc/groups")
public class GroupsViewController {
    @Autowired
    private GroupsDetailsService repository;

    @Autowired
    private PersonDetailsService personRepository;

    @GetMapping("/group-tracker")
    @Transactional(readOnly = true)
    public String group(Authentication authentication, Model model) {
        List<Groups> groups = repository.listAll();
        model.addAttribute("groups", groups);
        List<Person> users = personRepository.listAll();  // Fetch all persons
        model.addAttribute("users", users);
        return "group/group";
    }
}