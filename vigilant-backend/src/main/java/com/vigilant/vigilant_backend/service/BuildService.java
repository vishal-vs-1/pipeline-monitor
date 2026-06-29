package com.vigilant.vigilant_backend.service;

import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BuildService {
    Map<String, List<BuildState>> getRecentBuildsGroupedByRepo();
    List<BuildState> getLast10Builds(TrackedRepo repo);
    Optional<BuildState> findByRepoAndRunId(TrackedRepo repo, Long runId);
    BuildState saveBuildState(BuildState buildState);
}
