package com.vigilant.vigilant_backend.service;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.entity.User;
import com.vigilant.vigilant_backend.dto.request.UpdateRepoRequest;

import java.util.List;

public interface RepoService {
    List<TrackedRepo> getAllRepos(User user);
    List<TrackedRepo> getActiveRepos();
    List<TrackedRepo> getActiveReposByUserEmails(java.util.Set<String> emails);
    TrackedRepo addRepo(TrackedRepo repo, User user);
    TrackedRepo updateRepo(Long id, UpdateRepoRequest request, User user);
    TrackedRepo findById(Long id, User user);
    void deleteRepo(Long id, User user);
}
