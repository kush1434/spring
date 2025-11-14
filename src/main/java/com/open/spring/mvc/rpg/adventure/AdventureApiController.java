package com.open.spring.mvc.rpg.adventure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.bank.Bank;
import com.open.spring.mvc.bank.BankJpaRepository;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;
 

import lombok.Data;

@RestController
@RequestMapping("/adventure")
public class AdventureApiController {

    @Autowired
    private AdventureJpaRepository adventureJpaRepository;
    @Autowired
    private PersonJpaRepository personJpaRepository;
    @Autowired
    private BankJpaRepository bankJpaRepository;
    

    @Data
    public static class AdventureCombinedDto {
        private Long personId;
        private Integer questionsAnswered;
        private Double questionAccuracy;
        private Long chatScore;
        private Double balance;
        private Boolean transitionToParadise;
        private List<Map<String, Object>> questions = new ArrayList<>();
    }

    @GetMapping("/combined/{personid}")
    public ResponseEntity<AdventureCombinedDto> getCombined(@PathVariable Long personid,
            @RequestParam(required = false) String category) {

        Optional<Person> personOpt = personJpaRepository.findById(personid);
        if (personOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Person person = personOpt.get();
        String uid = person.getUid();
        Bank bank = bankJpaRepository.findByUid(uid);

        AdventureCombinedDto dto = new AdventureCombinedDto();
        dto.setPersonId(personid);

        List<Adventure> useranswers = adventureJpaRepository.findByPersonId(personid);
        dto.setQuestionsAnswered(useranswers.size());

        double questionsAnswered = useranswers.size();
        double questionsRight = 0;
        long totalChatScore = 0L;
        for (Adventure a : useranswers) {
            if (Boolean.TRUE.equals(a.getAnswerIsCorrect())) questionsRight++;
            if (a.getChatScore() != null) totalChatScore += a.getChatScore();
        }
        dto.setChatScore(totalChatScore);
        dto.setQuestionAccuracy(questionsAnswered == 0 ? 0.0 : (questionsRight / questionsAnswered));

        dto.setBalance(bank == null ? 0.0 : bank.getBalance());

        // transitionToParadise: check Meteor category questions answered correctly
        boolean transitioned = true;
        List<Adventure> meteorAnswers = adventureJpaRepository.findByPersonIdAndQuestionCategory(personid, "Meteor");
        if (meteorAnswers.isEmpty()) transitioned = false;
        for (Adventure mq : meteorAnswers) {
            if (!Boolean.TRUE.equals(mq.getAnswerIsCorrect())) { transitioned = false; break; }
        }
        dto.setTransitionToParadise(transitioned);

        // questions + choices + rubric (optionally filter by category)
        List<Adventure> questions = (category == null) ? adventureJpaRepository.findAll() : adventureJpaRepository.findByQuestionCategory(category);
        for (Adventure q : questions) {
            Map<String, Object> m = new HashMap<>();
            m.put("adventureRow", q);
            dto.getQuestions().add(m);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

}
