package com.vigilant.vigilant_backend;

import com.vigilant.vigilant_backend.entity.User;
import com.vigilant.vigilant_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class VerifyDbTokenTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testTokens() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            System.out.println("=========================================");
            System.out.println("User ID: " + user.getId());
            System.out.println("User Email: " + user.getEmail());
            String token = user.getGithubToken();
            if (token != null && !token.isBlank()) {
                System.out.println("GitHub Token: " + token.substring(0, Math.min(10, token.length())) + "...");
                System.out.println("Token Length: " + token.length());
            } else {
                System.out.println("GitHub Token: (none)");
            }
            System.out.println("=========================================");
        }
    }
}
