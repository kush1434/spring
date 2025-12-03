package com.open.spring.mvc.leaderboard;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController // Changed from @Controller to @RestController
@RequestMapping("/api/gamer") // Changed to /api/gamer to follow REST conventions
public class GamerApiController {

    @Autowired
    private GamerJpaRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* GET List of All Players
     * @GetMapping annotation is used for mapping HTTP GET requests onto specific handler methods.
     */
    @GetMapping("/")
    public ResponseEntity<List<Gamer>> getPlayers() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    /* POST Register New Player
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerPlayer(@RequestBody PlayerRegistrationRequest request) {
        // Check if username already exists
        Optional<Gamer> existingPlayer = repository.findByUsername(request.getUsername());
        if (existingPlayer.isPresent()) {
            return new ResponseEntity<>("Username already exists!", HttpStatus.BAD_REQUEST);
        }

        // Create new player
        Gamer player = new Gamer();
        player.setUsername(request.getUsername());
        player.setPassword(passwordEncoder.encode(request.getPassword()));
        player.setRole("PLAYER");
        player.setEnabled(true);
        player.setHighScore(0);
        
        repository.save(player);
        return new ResponseEntity<>("Player registered successfully!", HttpStatus.OK);
    }

    /* POST Login Player
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginPlayer(@RequestBody PlayerLoginRequest request) {
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return new ResponseEntity<>("Password cannot be empty!", HttpStatus.BAD_REQUEST);
        }
    
        Optional<Gamer> playerOptional = repository.findByUsername(request.getUsername());
        if (playerOptional.isPresent()) {
            Gamer player = playerOptional.get();
            if (passwordEncoder.matches(request.getPassword(), player.getPassword())) {
                return new ResponseEntity<>("Login successful!", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Invalid username or password!", HttpStatus.UNAUTHORIZED);
    }

    /* POST Update Player Score
     * @PathVariable annotation can be used if you want to pass username in URL
     */
    @PostMapping("/score")
    public ResponseEntity<Gamer> updateScore(@RequestBody ScoreUpdateRequest request) {
        Optional<Gamer> optional = repository.findByUsername(request.getUsername());
        if (optional.isPresent()) {
            Gamer player = optional.get();
            // Only update if the new score is higher than the current high score
            if (request.getScore() > player.getHighScore()) {
                player.setHighScore(request.getScore());
                repository.save(player);
            }
            return new ResponseEntity<>(player, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /* GET Player Score by Username
     */
    @GetMapping("/score/{username}")
    public ResponseEntity<Integer> getScore(@PathVariable String username) {
        Optional<Gamer> optional = repository.findByUsername(username);
        if (optional.isPresent()) {
            return new ResponseEntity<>(optional.get().getHighScore(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /* GET Leaderboard - Top 10 Players
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        List<LeaderboardEntry> leaderboard = repository.findAll().stream()
                .sorted(Comparator.comparingInt(Gamer::getHighScore).reversed())
                .limit(10)
                .map(player -> new LeaderboardEntry(player.getUsername(), player.getHighScore()))
                .collect(Collectors.toList());
        
        return new ResponseEntity<>(leaderboard, HttpStatus.OK);
    }
}

// Request/Response DTOs
@Data
@NoArgsConstructor
@AllArgsConstructor
class LeaderboardEntry {
    private String username;
    private int highScore;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerRegistrationRequest {
    private String username;
    private String password;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerLoginRequest {
    private String username;
    private String password;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ScoreUpdateRequest {
    private String username;
    private int score;
}