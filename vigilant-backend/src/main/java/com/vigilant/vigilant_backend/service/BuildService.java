package com.vigilant.vigilant_backend.service;

import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vigilant.vigilant_backend.dto.response.RepoBuildsResponse;

import java.util.List;
import java.util.Optional;

public interface BuildService {
    List<RepoBuildsResponse> getRecentBuildsGroupedByRepo();
    Page<BuildState> getBuildsForRepo(Long repoId, Pageable pageable);
    List<BuildState> getLast10Builds(TrackedRepo repo);
    Optional<BuildState> findByRepoAndRunId(TrackedRepo repo, Long runId);
    BuildState saveBuildState(BuildState buildState);
}
