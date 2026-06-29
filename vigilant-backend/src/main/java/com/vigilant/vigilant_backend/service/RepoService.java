package com.vigilant.vigilant_backend.service;

import com.vigilant.vigilant_backend.entity.TrackedRepo;

import java.util.List;

public interface RepoService {
    List<TrackedRepo> getAllRepos();
    List<TrackedRepo> getActiveRepos();
    TrackedRepo addRepo(TrackedRepo repo);
    TrackedRepo findById(Long id);
}
