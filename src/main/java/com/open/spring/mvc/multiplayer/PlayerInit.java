package com.open.spring.mvc.multiplayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

@Configuration
public class PlayerInit {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initGamers(PlayerRepository repository) {
        return args -> {

            if (repository.count() == 0) {

                String[] usernames = Player.init();
                Random random = new Random();

                for (String username : usernames) {

                    Player player = new Player();
                    player.setUsername(username);
                    player.setPassword(passwordEncoder.encode("password123"));
                    player.setRole("PLAYER");
                    player.setEnabled(true);

                    String location = random.nextInt(1001) + "," + random.nextInt(1001);
                    player.setLocation(location);

                    repository.save(player);
                }

                System.out.println("Gamer database initialized with "
                        + usernames.length + " players (level + XY coords)");
            }
        };
    }
}
