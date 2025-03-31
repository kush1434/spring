// define package and import necessary libraries
package com.nighthawk.spring_portfolio.mvc.rpg.adventureGameStat;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.rpg.gamifyGame.Game;
import com.nighthawk.spring_portfolio.mvc.rpg.gamifyGame.GameJpaRepository;

import lombok.Data;



// define rest controller and set base endpoint mapping
@RestController
@RequestMapping("/game")
public class GameStatApiController {

    @Autowired
    private GameJpaRepository gameJpaRepository;
    @Autowired
    private PersonJpaRepository personJpaRepository;
    @Autowired
    private GameStatJpaRepository gameStatJpaRepository;

    @Data
    public static class GameStatsDTO {
        private String uid;  
        private String gname;     
        private Map<String, Object> stats; 
    }

    @PostMapping("/createStats")
    public ResponseEntity<Map<String, Object>> createGameStat(@RequestBody GameStatsDTO dto) {
        Person person = personJpaRepository.findByUid(dto.getUid());
        if (person == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Game game = gameJpaRepository.findByName(dto.getGname());
        if (game == null) {
            game = gameJpaRepository.save(new Game(dto.getGname(), person));
        }
        
        GameStat gameStat = GameStat.createGameStats(person, game, dto.getStats());
        Map<String, Object> stats = gameStat.getStats();
    
        gameStatJpaRepository.save(gameStat);
        return new ResponseEntity<>(stats, HttpStatus.CREATED);
    }
    


    @GetMapping("/getStats/{uid}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable String uid) {
        GameStat gamestat = gameStatJpaRepository.findByUid_uid(uid);
        
        if (gamestat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Map<String, Object> stats = gamestat.getStats();
        
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @PutMapping("/updateStats")
    public ResponseEntity<Map<String, Object>> updateStats(@RequestBody GameStatsDTO dto) {
        GameStat gamestat = gameStatJpaRepository.findByUid_uid(dto.getUid());
        
        if (gamestat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        gamestat.setStats(dto.getStats());
        
        gameStatJpaRepository.save(gamestat);
        
        return new ResponseEntity<>(gamestat.getStats(), HttpStatus.OK);
    }
}