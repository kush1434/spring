package com.open.spring.mvc.quests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Optional;

@RestController
@RequestMapping("/api/quests")
public class QuestsController {

    @Autowired
    private QuestsRepository repository;

    @GetMapping("/{id}")
    public ResponseEntity<Quest> getQuest(@PathVariable Long id) {
        Optional<Quest> quest = repository.findById(id);
        return ResponseEntity.of(quest);
    }

    @PostMapping("/create")
    public ResponseEntity<Quest> createQuest(@RequestBody String entity) {
        // For simplicity, let's assume the entity is valid and create a dummy quest
        Quest newQuest = new Quest("Sample Quest", Quest.Difficulty.MEDIUM, "/sample-quest", 5, 100);
        repository.save(newQuest);
        return new ResponseEntity<>(newQuest, HttpStatus.CREATED);
    }
    
}
