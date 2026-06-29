package com.vigilant.vigilant_backend;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.repository.TrackedRepoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class VerifyDbTokenTest {

    @Autowired
    private TrackedRepoRepository repoRepository;

    @Test
    public void testTokens() {
        List<TrackedRepo> repos = repoRepository.findAll();
        for (TrackedRepo repo : repos) {
            System.out.println("=========================================");
            System.out.println("Repo ID: " + repo.getId());
            System.out.println("Repo Name: " + repo.getRepoName());
            System.out.println("Decrypted Token: " + repo.getGithubToken().substring(0, Math.min(10, repo.getGithubToken().length())) + "...");
            System.out.println("Decrypted Token Length: " + repo.getGithubToken().length());
            System.out.println("=========================================");
        }
    }
}
