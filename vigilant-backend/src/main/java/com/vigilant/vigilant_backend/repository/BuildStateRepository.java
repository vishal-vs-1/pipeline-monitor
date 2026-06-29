package com.vigilant.vigilant_backend.repository;

import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuildStateRepository extends JpaRepository<BuildState, Long> {
    Optional<BuildState> findByRepoAndRunId(TrackedRepo repo, Long runId);
    List<BuildState> findTop5ByRepoOrderByRunIdDesc(TrackedRepo repo);
    List<BuildState> findTop10ByRepoOrderByRunIdDesc(TrackedRepo repo);
}
