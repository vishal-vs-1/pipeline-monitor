package com.vigilant.vigilant_backend.service.impl;

import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.repository.BuildStateRepository;
import com.vigilant.vigilant_backend.repository.TrackedRepoRepository;
import com.vigilant.vigilant_backend.service.BuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vigilant.vigilant_backend.dto.response.RepoBuildsResponse;
import com.vigilant.vigilant_backend.dto.response.BuildStateResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildServiceImpl implements BuildService {

    private final BuildStateRepository buildStateRepository;
    private final TrackedRepoRepository repoRepository;

    @Override
    public List<RepoBuildsResponse> getRecentBuildsGroupedByRepo() {
        List<RepoBuildsResponse> recentBuilds = new ArrayList<>();
        List<TrackedRepo> activeRepos = repoRepository.findByIsActiveTrueOrderByIdAsc();
        
        for (TrackedRepo repo : activeRepos) {
            List<BuildState> top5 = buildStateRepository.findTop5ByRepoOrderByRunIdDesc(repo);
            List<BuildStateResponse> buildResponses = top5.stream()
                    .map(BuildStateResponse::fromEntity)
                    .toList();
            recentBuilds.add(new RepoBuildsResponse(repo.getId(), repo.getRepoName(), repo.getBranch(), buildResponses));
        }
        
        return recentBuilds;
    }

    @Override
    public Page<BuildState> getBuildsForRepo(Long repoId, Pageable pageable) {
        TrackedRepo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found"));
        return buildStateRepository.findByRepoOrderByRunIdDesc(repo, pageable);
    }

    @Override
    public List<BuildState> getLast10Builds(TrackedRepo repo) {
        return buildStateRepository.findTop10ByRepoOrderByRunIdDesc(repo);
    }

    @Override
    public Optional<BuildState> findByRepoAndRunId(TrackedRepo repo, Long runId) {
        return buildStateRepository.findByRepoAndRunId(repo, runId);
    }

    @Override
    public BuildState saveBuildState(BuildState buildState) {
        return buildStateRepository.save(buildState);
    }
}
