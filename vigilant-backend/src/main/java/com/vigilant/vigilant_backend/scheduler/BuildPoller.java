package com.vigilant.vigilant_backend.scheduler;

import com.vigilant.vigilant_backend.dto.WorkflowRun;
import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.kafka.BuildEvent;
import com.vigilant.vigilant_backend.service.BuildService;
import com.vigilant.vigilant_backend.service.RepoService;
import com.vigilant.vigilant_backend.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BuildPoller {

    private final RepoService repoService;
    private final BuildService buildService;
    private final GithubService githubService;
    private final KafkaTemplate<String, BuildEvent> kafkaTemplate;

    // Memory cache of runs we already know are completed to save DB queries
    private final Set<Long> knownCompletedRuns = ConcurrentHashMap.newKeySet();

    /**
     * Scheduled job that periodically polls GitHub for the latest workflow runs
     * of all active tracked repositories. If a state change is detected,
     * it publishes an event to Kafka.
     */
    @Scheduled(fixedDelayString = "${poller.delay}")
    public void pollRepositories() {
        List<TrackedRepo> activeRepos = repoService.getActiveRepos();
        Set<Long> currentCycleRunIds = ConcurrentHashMap.newKeySet();

        for (TrackedRepo repo : activeRepos) {
            List<WorkflowRun> recentRuns = githubService.getRecentRuns(
                    repo.getRepoName(),
                    repo.getBranch(),
                    repo.getGithubToken());

            for (WorkflowRun run : recentRuns) {
                currentCycleRunIds.add(run.id());
                processWorkflowRun(repo, run);
            }
        }

        // Prevent memory leak by removing any runIds that are no longer in the recent
        // runs from GitHub
        knownCompletedRuns.retainAll(currentCycleRunIds);
    }

    /**
     * Processes a single workflow run retrieved from GitHub.
     * Calculates duration and delegates to state handler methods based on whether
     * this build run has been seen before in the database.
     */
    private void processWorkflowRun(TrackedRepo repo, WorkflowRun latestRun) {
        // If we already verified this run is completed in previous cycles, completely
        // ignore it!
        if (knownCompletedRuns.contains(latestRun.id())) {
            return;
        }

        Optional<BuildState> existingStateOpt = buildService.findByRepoAndRunId(repo, latestRun.id());
        Integer durationSeconds = calculateDuration(latestRun);

        if (existingStateOpt.isEmpty()) {
            handleNewBuildState(repo, latestRun, durationSeconds);
        } else {
            handleExistingBuildState(repo, latestRun, durationSeconds, existingStateOpt.get());
        }
    }

    /**
     * Calculates the execution duration of a workflow run in seconds.
     */
    private Integer calculateDuration(WorkflowRun run) {
        if (run.createdAt() != null && run.updatedAt() != null) {
            return (int) Duration.between(run.createdAt(), run.updatedAt()).getSeconds();
        }
        return null;
    }

    /**
     * Handles the case where a completely new workflow run is detected.
     * Saves it to the database and immediately fires a Kafka event.
     */
    private void handleNewBuildState(TrackedRepo repo, WorkflowRun latestRun, Integer durationSeconds) {
        BuildState newState = new BuildState();
        newState.setRepo(repo);
        newState.setRunId(latestRun.id());
        newState.setStatus(latestRun.status());
        newState.setConclusion(latestRun.conclusion());
        newState.setDurationSeconds(durationSeconds);

        buildService.saveBuildState(newState);
        publishEvent(repo, latestRun, durationSeconds);

        if ("completed".equalsIgnoreCase(latestRun.status())) {
            knownCompletedRuns.add(latestRun.id());
        }
    }

    /**
     * Handles an existing workflow run. If the status (e.g. in_progress ->
     * completed)
     * or conclusion (e.g. success -> failure) has changed, updates the DB and fires
     * a Kafka event.
     */
    private void handleExistingBuildState(TrackedRepo repo, WorkflowRun latestRun, Integer durationSeconds,
            BuildState existingState) {
        boolean statusChanged = !String.valueOf(existingState.getStatus()).equals(String.valueOf(latestRun.status()));
        boolean conclusionChanged = !String.valueOf(existingState.getConclusion())
                .equals(String.valueOf(latestRun.conclusion()));

        // If the build is still running, the duration will constantly increase. We
        // should update the DB and notify UI.
        boolean isRunningAndDurationChanged = "in_progress".equalsIgnoreCase(latestRun.status())
                && (existingState.getDurationSeconds() == null
                        || !existingState.getDurationSeconds().equals(durationSeconds));

        if (statusChanged || conclusionChanged || isRunningAndDurationChanged) {
            existingState.setStatus(latestRun.status());
            existingState.setConclusion(latestRun.conclusion());
            existingState.setDurationSeconds(durationSeconds);

            buildService.saveBuildState(existingState);
            publishEvent(repo, latestRun, durationSeconds);
        }

        if ("completed".equalsIgnoreCase(latestRun.status())) {
            knownCompletedRuns.add(latestRun.id());
        }
    }

    /**
     * Constructs a BuildEvent record and publishes it to the 'build-events' Kafka
     * topic.
     * This acts as the shock-absorber for downstream services.
     */
    private void publishEvent(TrackedRepo repo, WorkflowRun latestRun, Integer durationSeconds) {
        BuildEvent event = new BuildEvent(
                repo.getId(),
                repo.getRepoName() + " (" + repo.getBranch() + ")",
                latestRun.id(),
                latestRun.status(),
                latestRun.conclusion(),
                durationSeconds,
                false);
        kafkaTemplate.send("build-events", repo.getRepoName(), event);
    }
}
