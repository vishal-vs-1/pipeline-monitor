package com.vigilant.vigilant_backend.service.impl;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.repository.TrackedRepoRepository;
import com.vigilant.vigilant_backend.service.RepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public TrackedRepo findById(Long id) {
        return repoRepository.findById(id).orElse(null);
    }
}
