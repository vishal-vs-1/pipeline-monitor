package com.vigilant.vigilant_backend.service.impl;

import com.vigilant.vigilant_backend.dto.WorkflowRun;
import com.vigilant.vigilant_backend.dto.WorkflowRunsResponse;
import com.vigilant.vigilant_backend.service.GithubService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
        try {
            String[] parts = repoName.split("/");
            if (parts.length != 2) {
                System.err.println("Invalid repository name format. Expected 'owner/repo', got: " + repoName);
                return List.of();
            }
            String owner = parts[0];
            String repo = parts[1];

            WorkflowRunsResponse response = restClient.get()
                    .uri("/repos/{owner}/{repo}/actions/runs?branch={branch}&per_page=5", owner, repo, branch)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(WorkflowRunsResponse.class);

            if (response != null && response.workflowRuns() != null) {
                return response.workflowRuns();
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Error fetching runs for repo " + repoName + ": " + e.getMessage());
        }
        return List.of();
    }
}
