package com.open.spring.mvc.multiplayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
@CrossOrigin
public class PlayerController {
    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping("/connect")
    public Player connect(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        
        Player player = playerRepository.findByUsername(username)
            .orElse(new Player(username, "online"));
        
        player.setStatus("online");
        player.setLastActive(LocalDateTime.now());
        player.setConnectedAt(LocalDateTime.now());
        
        return playerRepository.save(player);
    }

    @GetMapping("/online")
    public Map<String, Object> getOnlinePlayers() {
        List<Player> onlinePlayers = playerRepository.findByStatus("online");
        return Map.of("players", onlinePlayers);
    }

    @PutMapping("/status")
    public Player updateStatus(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String status = request.get("status");
        
        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        player.setStatus(status);
        player.setLastActive(LocalDateTime.now());
        
        return playerRepository.save(player);
    }

    @PostMapping("/disconnect")
    public void disconnect(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        
        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        player.setStatus("offline");
        playerRepository.save(player);
    }
    @PutMapping("/location")
    public Player updateLocation(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        double x = (double) request.get("x");
        double y = (double) request.get("y");

        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setX(x);
        player.setY(y);
        player.setLastActive(LocalDateTime.now());

        return playerRepository.save(player);
    }
    @PutMapping("/level")
        public Player updateLevel(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int level = (int) request.get("level");

        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setLevel(level);
        player.setLastActive(LocalDateTime.now());

        return playerRepository.save(player);
    }      

    // for multiplayer locations if needed:
    @GetMapping("/locations")
    public Map<String, Object> getPlayerLocations() {
        List<Player> players = playerRepository.findByStatus("online");

        return Map.of("players", players.stream().map(p -> Map.of(
                "username", p.getUsername(),
                "x", p.getX(),
                "y", p.getY(),
                "level", p.getLevel()
        )).toList());
    }

}