package com.vigilant.vigilant_backend.service;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.dto.request.UpdateRepoRequest;

import java.util.List;

public interface RepoService {
    List<TrackedRepo> getAllRepos();
    List<TrackedRepo> getActiveRepos();
    TrackedRepo addRepo(TrackedRepo repo);
    TrackedRepo updateRepo(Long id, UpdateRepoRequest request);
    TrackedRepo findById(Long id);
}
