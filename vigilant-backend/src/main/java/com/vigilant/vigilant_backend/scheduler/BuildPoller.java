package com.vigilant.vigilant_backend.scheduler;

import com.vigilant.vigilant_backend.dto.WorkflowRun;
import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.kafka.BuildEvent;
import com.vigilant.vigilant_backend.service.BuildService;
import com.vigilant.vigilant_backend.service.RepoService;
import com.vigilant.vigilant_backend.service.GithubService;
import com.vigilant.vigilant_backend.service.WebSocketPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildPoller {

    private final RepoService repoService;
    private final BuildService buildService;
    private final GithubService githubService;
    private final KafkaTemplate<String, BuildEvent> kafkaTemplate;
    private final WebSocketPresenceService presenceService;

    // Memory cache of runs we already know are completed, scoped per user email
    private final Map<String, Set<Long>> knownCompletedRuns = new ConcurrentHashMap<>();

    /**
     * Scheduled job that periodically polls GitHub for the latest workflow runs
     * of all active tracked repositories. If a state change is detected,
     * it publishes an event to Kafka.
     */
    @Scheduled(fixedDelayString = "${poller.delay}")
    public void pollRepositories() {
        Set<String> onlineUsers = presenceService.getOnlineUserEmails();
        if (onlineUsers.isEmpty()) {
            log.info("0 online users. Skipping poll cycle.");
            return;
        }

        // Users who just came online need a bigger fetch to backfill missed builds
        Set<String> newUsers = presenceService.consumeNewlyConnectedUsers();

        List<TrackedRepo> activeRepos = repoService.getActiveReposByUserEmails(onlineUsers);
        Set<Long> currentCycleRunIds = ConcurrentHashMap.newKeySet();

        // Track current cycle run IDs per user for cleanup
        Map<String, Set<Long>> currentCycleRunIdsByUser = new ConcurrentHashMap<>();

        for (TrackedRepo repo : activeRepos) {
            String token = repo.getUser().getGithubToken();
            String userEmail = repo.getUser().getEmail();

            // Backfill: 30 runs for new users, 5 for already-polling users
            int perPage = newUsers.contains(userEmail) ? 30 : 5;

            List<WorkflowRun> recentRuns = githubService.getRecentRuns(
                    repo.getRepoName(),
                    repo.getBranch(),
                    token,
                    perPage);

            Set<Long> userCycleRunIds = currentCycleRunIdsByUser
                    .computeIfAbsent(userEmail, k -> ConcurrentHashMap.newKeySet());

            for (WorkflowRun run : recentRuns) {
                userCycleRunIds.add(run.id());
                processWorkflowRun(repo, run, userEmail);
            }
        }

        // Prevent memory leak: retain only run IDs from the current cycle per user
        for (Map.Entry<String, Set<Long>> entry : currentCycleRunIdsByUser.entrySet()) {
            Set<Long> userCache = knownCompletedRuns.get(entry.getKey());
            if (userCache != null) {
                userCache.retainAll(entry.getValue());
            }
        }
        // Evict cache for users who went offline mid-cycle
        knownCompletedRuns.keySet().retainAll(onlineUsers);
    }

    /**
     * Processes a single workflow run retrieved from GitHub.
     * Calculates duration and delegates to state handler methods based on whether
     * this build run has been seen before in the database.
     */
    private void processWorkflowRun(TrackedRepo repo, WorkflowRun latestRun, String userEmail) {
        // If we already verified this run is completed in previous cycles, completely
        // ignore it!
        Set<Long> userCache = knownCompletedRuns.get(userEmail);
        if (userCache != null && userCache.contains(latestRun.id())) {
            return;
        }

        Optional<BuildState> existingStateOpt = buildService.findByRepoAndRunId(repo, latestRun.id());
        Integer durationSeconds = calculateDuration(latestRun);

        if (existingStateOpt.isEmpty()) {
            handleNewBuildState(repo, latestRun, durationSeconds, userEmail);
        } else {
            handleExistingBuildState(repo, latestRun, durationSeconds, existingStateOpt.get(), userEmail);
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
    private void handleNewBuildState(TrackedRepo repo, WorkflowRun latestRun, Integer durationSeconds, String userEmail) {
        BuildState newState = new BuildState();
        newState.setRepo(repo);
        newState.setRunId(latestRun.id());
        newState.setStatus(latestRun.status());
        newState.setConclusion(latestRun.conclusion());
        newState.setDurationSeconds(durationSeconds);

        buildService.saveBuildState(newState);
        publishEvent(repo, latestRun, durationSeconds);

        if ("completed".equalsIgnoreCase(latestRun.status())) {
            knownCompletedRuns.computeIfAbsent(userEmail, k -> ConcurrentHashMap.newKeySet()).add(latestRun.id());
        }
    }

    /**
     * Handles an existing workflow run. If the status (e.g. in_progress ->
     * completed)
     * or conclusion (e.g. success -> failure) has changed, updates the DB and fires
     * a Kafka event.
     */
    private void handleExistingBuildState(TrackedRepo repo, WorkflowRun latestRun, Integer durationSeconds,
            BuildState existingState, String userEmail) {
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
            knownCompletedRuns.computeIfAbsent(userEmail, k -> ConcurrentHashMap.newKeySet()).add(latestRun.id());
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
