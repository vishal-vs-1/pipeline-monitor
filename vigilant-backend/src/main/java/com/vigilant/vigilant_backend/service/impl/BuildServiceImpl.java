package com.vigilant.vigilant_backend.service.impl;

import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.repository.BuildStateRepository;
import com.vigilant.vigilant_backend.repository.TrackedRepoRepository;
import com.vigilant.vigilant_backend.service.BuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuildServiceImpl implements BuildService {

    private final BuildStateRepository buildStateRepository;
    private final TrackedRepoRepository repoRepository;

    @Override
    public Map<String, List<BuildState>> getRecentBuildsGroupedByRepo() {
        Map<String, List<BuildState>> recentBuilds = new HashMap<>();
        List<TrackedRepo> activeRepos = repoRepository.findByIsActiveTrue();
        
        for (TrackedRepo repo : activeRepos) {
            List<BuildState> top5 = buildStateRepository.findTop5ByRepoOrderByRunIdDesc(repo);
            String displayKey = repo.getRepoName() + " (" + repo.getBranch() + ")";
            recentBuilds.put(displayKey, top5);
        }
        
        return recentBuilds;
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
