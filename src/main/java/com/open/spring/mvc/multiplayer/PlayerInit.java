package com.open.spring.mvc.multiplayer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Random;

@Configuration
public class PlayerInit {

    @Bean
    CommandLineRunner initGamers(PlayerRepository repository) {
        return args -> {

            if (repository.count() == 0) {

                // create a set of default usernames
                int count = 50;
                String[] usernames = new String[count];
                for (int i = 0; i < count; i++) {
                    usernames[i] = "player" + (i + 1);
                }

                Random random = new Random();

                for (String username : usernames) {
                    Player player = new Player();
                    player.setUsername(username);
                    // set a default status and random position/level
                    player.setStatus("online");
                    player.setLevel(random.nextInt(10) + 1); // levels 1-10
                    double x = random.nextDouble() * 1000.0;
                    double y = random.nextDouble() * 1000.0;
                    player.setX(x);
                    player.setY(y);
                    player.setLastActive(LocalDateTime.now());
                    player.setConnectedAt(LocalDateTime.now());

                    repository.save(player);
                }

                System.out.println("Gamer database initialized with "
                        + usernames.length + " players (level + XY coords)");
            }
        };
    }
}
