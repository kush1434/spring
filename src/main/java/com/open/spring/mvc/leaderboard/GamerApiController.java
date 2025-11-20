package com.open.spring.mvc.leaderboard;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Controller
@RequestMapping("/gamer")
public class GamerApiController {
    @Autowired
    private PlayerService playerService;

    @PostMapping("/register")
    @ResponseBody
    public String registerPlayer(@RequestBody PlayerRegistrationRequest request) {
        if (playerService.registerPlayer(request.getUsername(), request.getPassword()) != null) {
            return "Player registered successfully!";
        }
        return "Registration failed!";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> loginPlayer(@RequestBody PlayerLoginRequest request) {
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Password cannot be empty!");
        }
    
        Optional<Gamer> playerOptional = playerService.findByUsername(request.getUsername());
        if (playerOptional.isPresent()) {
            Gamer player = playerOptional.get();
            if (playerService.checkPassword(request.getPassword(), player.getPassword())) {
                return ResponseEntity.ok("Redirecting to game");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password!");
    }

    @PostMapping("/updateScore")
    @ResponseBody
    public ResponseEntity<String> updateScore(@RequestBody ScoreUpdateRequest request) {
        try {
            playerService.updateHighScore(request.getUsername(), request.getScore());
            return ResponseEntity.ok("Score updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/getScore")
    @ResponseBody
    public ResponseEntity<Integer> getScore(@RequestParam String username) {
        try {
            int highScore = playerService.getHighScore(username);
            return ResponseEntity.ok(highScore);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(null);
        }
    }

    @GetMapping("/leaderboard")
    @ResponseBody
    public List<LeaderboardEntry> getLeaderboard() {
        return playerService.getTopPlayersByScore();
    }
}

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

@Service
class PlayerService implements UserDetailsService {
    @Autowired
    private GamerJpaRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        Gamer player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Player not found"));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + player.getRole()));

        return org.springframework.security.core.userdetails.User
                .withUsername(player.getUsername())
                .password(player.getPassword())
                .authorities(authorities)
                .accountLocked(!player.isEnabled())
                .build();
    }

    public Gamer registerPlayer(String username, String password) {
        Gamer player = new Gamer();
        player.setUsername(username);
        player.setPassword(passwordEncoder.encode(password));
        player.setHighScore(0);
        return playerRepository.save(player);
    }

    public Optional<Gamer> findByUsername(String username) {
        return playerRepository.findByUsername(username);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void updateHighScore(String username, int score) {
        Gamer player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        // Only update if the new score is higher than the current high score
        if (score > player.getHighScore()) {
            player.setHighScore(score);
            playerRepository.save(player);
        }
    }

    public int getHighScore(String username) {
        Gamer player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        return player.getHighScore();
    }

    public List<LeaderboardEntry> getTopPlayersByScore() {
        return playerRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Gamer::getHighScore).reversed())
                .limit(10)
                .map(player -> new LeaderboardEntry(player.getUsername(), player.getHighScore()))
                .collect(Collectors.toList());
    }
}