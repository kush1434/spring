package com.open.spring.tools;

import com.open.spring.Main;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TobyLoginRunner {
    public static void main(String[] args) {
        // Start Spring without the web server
        ConfigurableApplicationContext context = new SpringApplicationBuilder(Main.class)
                .web(WebApplicationType.NONE)
                .run(args);
        try {
            PersonJpaRepository personRepository = context.getBean(PersonJpaRepository.class);
            PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

            // Load password from .env (fallback to a common default if missing)
            String adminPassword;
            try {
                Dotenv dotenv = Dotenv.load();
                adminPassword = dotenv.get("ADMIN_PASSWORD");
            } catch (Exception e) {
                adminPassword = null;
            }
            if (adminPassword == null || adminPassword.isBlank()) {
                adminPassword = "admin123";
            }

            String uid = "toby";
            Person toby = personRepository.findByUid(uid);
            if (toby == null) {
                System.out.println("User 'toby' not found");
                return;
            }

            boolean matches = passwordEncoder.matches(adminPassword, toby.getPassword());
            if (matches) {
                System.out.println("Login OK for 'toby'");
            } else {
                System.out.println("Login FAILED for 'toby' (wrong password)");
            }

            System.out.println("Press Enter to exit...");
            try { System.in.read(); } catch (Exception ignored) {}
        } finally {
            context.close();
        }
    }
}


