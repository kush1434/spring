package com.open.spring.mvc.user;

import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role = "USER";
    private boolean enabled = true;
    public double balance;
    private String stonks;

    public User(String username, String password, String role, boolean enabled, double balance, String stonks) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.balance = balance;
        this.stonks = stonks;
        }

    public static User createUser(String username, String password, String role, boolean enabled, double balance, String stonks) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setEnabled(enabled);
        user.setBalance(balance);
        user.setStonks(stonks);
        return user;
    }

    public static User[] init() {
        ArrayList<User> users = new ArrayList<>();
        
        
        users.add(createUser("Aidan Lau", "123lau", "USER", true, 50000.0, "2-AAPL,3-QCOM,3-V"));
        users.add(createUser("Saathvik G", "123gampa", "USER", true, 50000.0, "2-AAPL,3-QCOM,3-V"));
        users.add(createUser("Sri S", "123ss", "USER", true, 50000.0, "2-AAPL,3-QCOM,3-V"));

        return users.toArray(new User[0]);
    }
}