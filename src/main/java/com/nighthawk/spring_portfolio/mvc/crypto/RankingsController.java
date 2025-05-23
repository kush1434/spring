package com.nighthawk.spring_portfolio.mvc.crypto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;

@RestController
@RequestMapping("/api/rankings")
public class RankingsController {

    @Autowired
    private PersonJpaRepository repository;

    /**
     * Get top 10 users by balance.
     * 
     * @return Rankings of users.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers() {
        // Fetch all people from the database
        List<Person> people = repository.findAll();

        // Sort users by balance (descending) and take the top 10
        List<Map<String, Object>> topUsers = people.stream()
                .sorted(Comparator.comparingDouble(Person::getBalanceDouble).reversed())
                .limit(10)
                .map(person -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", person.getId());
                    userMap.put("name", person.getName());
                    userMap.put("balance", person.getBalance());
                    return userMap;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(topUsers, HttpStatus.OK);
    }

    // /**
    //  * Get teams ranked by total balance.
    //  * 
    //  * @return Rankings of teams.
    //  */
    // @GetMapping("/teamRankings")
    // public ResponseEntity<List<Map<String, Object>>> getTeamRankings() {
    //     // Fetch all people from the database
    //     List<Person> people = repository.findAll();

    //     // Group by team and calculate team balances
    //     Map<String, Double> teamBalances = new HashMap<>();
    //     for (Person person : people) {
    //         String team = person.getTeam();
    //         if (team != null && !team.isEmpty()) {
    //             teamBalances.put(team, teamBalances.getOrDefault(team, 0.0) + person.getBalance());
    //         }
    //     }

    //     // Sort teams by total balance (descending)
    //     List<Map<String, Object>> rankedTeams = teamBalances.entrySet().stream()
    //             .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
    //             .map(entry -> {
    //                 Map<String, Object> teamMap = new HashMap<>();
    //                 teamMap.put("team", entry.getKey());
    //                 teamMap.put("totalBalance", entry.getValue());
    //                 return teamMap;
    //             })
    //             .collect(Collectors.toList());

    //     return new ResponseEntity<>(rankedTeams, HttpStatus.OK);
    // }

    // /**
    //  * Placeholder for future endpoint.
    //  */
    // @GetMapping("/placeholder")
    // public ResponseEntity<String> placeholderEndpoint() {
    //     return new ResponseEntity<>("This endpoint is not implemented yet.", HttpStatus.OK);
    // }
}