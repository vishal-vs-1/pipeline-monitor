package com.vigilant.vigilant_backend.kafka.service.impl;

import com.vigilant.vigilant_backend.entity.BuildMetric;
import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.kafka.BuildEvent;
import com.vigilant.vigilant_backend.repository.BuildMetricRepository;
import com.vigilant.vigilant_backend.repository.BuildStateRepository;
import com.vigilant.vigilant_backend.repository.TrackedRepoRepository;
import com.vigilant.vigilant_backend.kafka.service.AnomalyDetectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyDetectorServiceImpl implements AnomalyDetectorService {

    private final BuildStateRepository buildStateRepository;
    private final BuildMetricRepository buildMetricRepository;
    private final TrackedRepoRepository trackedRepoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Kafka Listener that consumes BuildEvents emitted by the BuildPoller.
     * It runs anomaly detection algorithms and broadcasts the event to the UI via WebSockets.
     */
    @Override
    @KafkaListener(topics = "build-events", groupId = "vigilant-group")
    public void consumeBuildEvent(BuildEvent event) {
        TrackedRepo repo = trackedRepoRepository.findById(event.repoId()).orElse(null);
        if (repo == null) return;

        boolean isAnomaly = false;
        if (event.durationSeconds() != null && event.durationSeconds() > 0) {
            isAnomaly = analyzeBuildMetrics(repo, event);
        }
        
        BuildEvent finalEvent = new BuildEvent(
            event.repoId(), event.repoName(), event.runId(), 
            event.status(), event.conclusion(), event.durationSeconds(), isAnomaly
        );
        
        // General Real-time push for UI updates so the frontend doesn't have to poll
        messagingTemplate.convertAndSend("/topic/builds", finalEvent);
    }

    /**
     * Analyzes recent build history to detect anomalies (performance degradation or flaky pipelines).
     * Saves updated metrics to the database and dispatches WebSocket alerts if necessary.
     */
    private boolean analyzeBuildMetrics(TrackedRepo repo, BuildEvent event) {
        int limit = repo.getAnomalyWindowSize();
        List<BuildState> lastBuilds = buildStateRepository.findByRepoOrderByRunIdDesc(repo, org.springframework.data.domain.PageRequest.of(0, limit + 1)).getContent();
        
        // Exclude the current run so it doesn't skew the historical data
        List<BuildState> historicalBuilds = lastBuilds.stream()
                .filter(state -> !state.getRunId().equals(event.runId()))
                .limit(limit)
                .toList();

        double avgDuration = calculateAverageDuration(historicalBuilds);
        if (avgDuration == 0) return false; // Not enough data

        BuildMetric metric = getOrCreateBuildMetric(repo);
        metric.setAvgDurationLast10(new BigDecimal(avgDuration).setScale(2, RoundingMode.HALF_UP));

        boolean isPerfAnomaly = checkPerformanceDegradation(repo, event, avgDuration);
        boolean isFlakyAnomaly = checkFlakyPipeline(repo, event, historicalBuilds, metric);
        boolean isAnomaly = isPerfAnomaly || isFlakyAnomaly;

        if (isAnomaly) {
            buildStateRepository.findByRepoAndRunId(repo, event.runId()).ifPresent(state -> {
                state.setIsAnomaly(true);
                buildStateRepository.save(state);
            });
        }

        buildMetricRepository.save(metric);
        return isAnomaly;
    }

    /**
     * Retrieves existing build metrics for a repo or initializes a new one.
     */
    private BuildMetric getOrCreateBuildMetric(TrackedRepo repo) {
        BuildMetric metric = buildMetricRepository.findById(repo.getId()).orElse(new BuildMetric());
        metric.setRepoId(repo.getId());
        return metric;
    }

    /**
     * Calculates the average duration in seconds of the provided builds.
     */
    private double calculateAverageDuration(List<BuildState> builds) {
        double sumDuration = 0;
        int count = 0;
        for (BuildState state : builds) {
            // Only include builds that have actually finished in our historical average!
            if (state.getDurationSeconds() != null && "completed".equalsIgnoreCase(state.getStatus())) {
                sumDuration += state.getDurationSeconds();
                count++;
            }
        }
        return count > 0 ? sumDuration / count : 0;
    }

    /**
     * Detects if the current build is anomalously slow.
     * Alert threshold: Current duration is 50% longer (1.5x) than the historical average.
     */
    private boolean checkPerformanceDegradation(TrackedRepo repo, BuildEvent event, double avgDuration) {
        double multiplier = repo.getAnomalyMultiplier();
        return event.durationSeconds() > (avgDuration * multiplier);
    }

    /**
     * Detects if the pipeline is repeatedly failing.
     * Looks at the last 5 builds. If there is more than 1 failure and the current build failed, it alerts.
     */
    private boolean checkFlakyPipeline(TrackedRepo repo, BuildEvent event, List<BuildState> historicalBuilds, BuildMetric metric) {
        int failureCount = 0;
        for (BuildState state : historicalBuilds.stream().limit(5).toList()) {
            if ("failure".equalsIgnoreCase(state.getConclusion())) {
                failureCount++;
            }
        }
        
        metric.setFailureCountLast5(failureCount);

        return "failure".equalsIgnoreCase(event.conclusion()) && failureCount > 1;
    }
}
