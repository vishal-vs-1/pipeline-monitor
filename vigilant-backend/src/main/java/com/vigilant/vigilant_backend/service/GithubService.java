package com.vigilant.vigilant_backend.service;

import com.vigilant.vigilant_backend.dto.WorkflowRun;

import java.util.List;

public interface GithubService {
    List<WorkflowRun> getRecentRuns(String repoName, String branch, String token);
    void validateRepoAndToken(String repoName, String branch, String token);
}
