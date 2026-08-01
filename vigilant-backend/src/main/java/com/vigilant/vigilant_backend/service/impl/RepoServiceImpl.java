package com.vigilant.vigilant_backend.service.impl;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.entity.User;
import com.vigilant.vigilant_backend.repository.BuildMetricRepository;
import com.vigilant.vigilant_backend.repository.BuildStateRepository;
import com.vigilant.vigilant_backend.repository.TrackedRepoRepository;
import com.vigilant.vigilant_backend.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;
import com.vigilant.vigilant_backend.service.GithubService;

import java.util.List;
import com.vigilant.vigilant_backend.dto.request.UpdateRepoRequest;

@Service
@RequiredArgsConstructor
public class RepoServiceImpl implements RepoService {

    private final TrackedRepoRepository repoRepository;
    private final BuildStateRepository buildStateRepository;
    private final BuildMetricRepository buildMetricRepository;
    private final GithubService githubService;

    @Override
    public List<TrackedRepo> getAllRepos(User user) {
        return repoRepository.findByUserOrderByIdAsc(user);
    }

    @Override
    public List<TrackedRepo> getActiveRepos() {
        return repoRepository.findByIsActiveTrueOrderByIdAsc();
    }

    @Override
    public List<TrackedRepo> getActiveReposByUserEmails(java.util.Set<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return repoRepository.findByIsActiveTrueAndUserEmailIn(emails);
    }

    @Override
    public TrackedRepo addRepo(TrackedRepo repo, User user) {
        repo.setUser(user);
        
        String tokenToUse = user.getGithubToken();
        if (tokenToUse == null || tokenToUse.isBlank()) {
            throw new IllegalArgumentException("GitHub token is required. Please link your GitHub account or set a token in your profile.");
        }
        
        githubService.validateRepoAndToken(repo.getRepoName(), repo.getBranch(), tokenToUse);
        
        return repoRepository.save(repo);
    }

    @Override
    public TrackedRepo updateRepo(Long id, UpdateRepoRequest request, User user) {
        TrackedRepo existing = repoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Repository not found with id: " + id));

        String tokenToValidate = user.getGithubToken();
        if (tokenToValidate == null || tokenToValidate.isBlank()) {
            throw new IllegalArgumentException("GitHub token is required.");
        }
        
        githubService.validateRepoAndToken(request.repoName(), request.branch(), tokenToValidate);

        existing.setRepoName(request.repoName());
        existing.setBranch(request.branch());
        existing.setIsActive(request.isActive());

        if (request.anomalyMultiplier() != null) {
            existing.setAnomalyMultiplier(request.anomalyMultiplier());
        }
        if (request.anomalyWindowSize() != null) {
            existing.setAnomalyWindowSize(request.anomalyWindowSize());
        }

        return repoRepository.save(existing);
    }

    @Override
    public TrackedRepo findById(Long id, User user) {
        return repoRepository.findByIdAndUser(id, user).orElse(null);
    }

    @Override
    @Transactional
    public void deleteRepo(Long id, User user) {
        TrackedRepo repo = repoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Repository not found with id: " + id));

        buildMetricRepository.deleteById(id);
        buildStateRepository.deleteByRepo(repo);

        repoRepository.delete(repo);
    }
}
