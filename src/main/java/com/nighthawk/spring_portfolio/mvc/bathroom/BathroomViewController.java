package com.nighthawk.spring_portfolio.mvc.bathroom;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;

@Controller
public class BathroomViewController {
    
    @Autowired
    private BathroomQueueJPARepository queueRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    // Using @PreAuthorize to restrict access to only those with ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/bathroomView")
    public String bathroomQueue(Model model) {
        List<BathroomQueue> queues = queueRepository.findAll();
        model.addAttribute("queues", queues);
        model.addAttribute("isAdmin", true);
        return "bathroomView";
    }
}