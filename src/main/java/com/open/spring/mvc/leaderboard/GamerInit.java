package com.open.spring.mvc.leaderboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

@Configuration
public class GamerInit {
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initGamers(GamerJpaRepository repository) {
        return args -> {
            // Only initialize if database is empty
            if (repository.count() == 0) {
                String[] usernames = Gamer.init();
                Random random = new Random();
                
                for (String username : usernames) {
                    Gamer player = new Gamer();
                    player.setUsername(username);
                    player.setPassword(passwordEncoder.encode("password123")); // Default password
                    player.setRole("PLAYER");
                    player.setEnabled(true);
                    player.setHighScore(random.nextInt(60000)); // Random score between 0-60000
                    repository.save(player);
                }
                
                System.out.println("Gamer database initialized with " + usernames.length + " players");
            }
        };
    }
}