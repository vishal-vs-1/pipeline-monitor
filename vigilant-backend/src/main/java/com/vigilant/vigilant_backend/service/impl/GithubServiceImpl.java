package com.vigilant.vigilant_backend.service.impl;

import com.vigilant.vigilant_backend.dto.WorkflowRun;
import com.vigilant.vigilant_backend.dto.WorkflowRunsResponse;
import com.vigilant.vigilant_backend.service.GithubService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class GithubServiceImpl implements GithubService {

    private final RestClient restClient;

    public GithubServiceImpl(@Value("${github.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }

    @Override
    public List<WorkflowRun> getRecentRuns(String repoName, String branch, String token) {
        return getRecentRuns(repoName, branch, token, 5);
    }

    @Override
    public List<WorkflowRun> getRecentRuns(String repoName, String branch, String token, int perPage) {
        try {
            String[] parts = repoName.split("/");
            if (parts.length != 2) {
                System.err.println("Invalid repository name format. Expected 'owner/repo', got: " + repoName);
                return List.of();
            }
            String owner = parts[0];
            String repo = parts[1];

            WorkflowRunsResponse response = restClient.get()
                    .uri("/repos/{owner}/{repo}/actions/runs?branch={branch}&per_page={perPage}", owner, repo, branch, perPage)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(WorkflowRunsResponse.class);

            if (response != null && response.workflowRuns() != null) {
                return response.workflowRuns();
            }
        } catch (Exception e) {
            System.err.println("Error fetching runs for repo " + repoName + ": " + e.getMessage());
        }
        return List.of();
    }

    @Override
    public void validateRepoAndToken(String repoName, String branch, String token) {
        String[] parts = repoName.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid repository name format. Expected 'owner/repo', got: " + repoName);
        }
        String owner = parts[0];
        String repo = parts[1];

        try {
            // Check if the repo exists and the token has access
            restClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalArgumentException("Invalid GitHub token. Unauthorized access.");
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("GitHub repository not found or token lacks permissions: " + repoName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to validate GitHub credentials: " + e.getMessage());
        }
    }
}
