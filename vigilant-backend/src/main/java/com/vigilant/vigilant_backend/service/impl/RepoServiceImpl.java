package com.vigilant.vigilant_backend.service.impl;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.repository.TrackedRepoRepository;
import com.vigilant.vigilant_backend.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import com.vigilant.vigilant_backend.dto.request.UpdateRepoRequest;

@Service
@RequiredArgsConstructor
public class RepoServiceImpl implements RepoService {

    private final TrackedRepoRepository repoRepository;

    @Override
    public List<TrackedRepo> getAllRepos() {
        return repoRepository.findAll();
    }

    @Override
    public List<TrackedRepo> getActiveRepos() {
        return repoRepository.findByIsActiveTrue();
    }

    @Override
    public TrackedRepo addRepo(TrackedRepo repo) {
        return repoRepository.save(repo);
    }

    @Override
    public TrackedRepo updateRepo(Long id, UpdateRepoRequest request) {
        TrackedRepo existing = repoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found with id: " + id));

        existing.setRepoName(request.repoName());
        existing.setBranch(request.branch());
        existing.setIsActive(request.isActive());

        if (request.githubToken() != null && !request.githubToken().isBlank()) {
            existing.setGithubToken(request.githubToken());
        }
        if (request.anomalyMultiplier() != null) {
            existing.setAnomalyMultiplier(request.anomalyMultiplier());
        }
        if (request.anomalyWindowSize() != null) {
            existing.setAnomalyWindowSize(request.anomalyWindowSize());
        }

        return repoRepository.save(existing);
    }

    @Override
    public TrackedRepo findById(Long id) {
        return repoRepository.findById(id).orElse(null);
    }
}
