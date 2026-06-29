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

        if (event.durationSeconds() != null && event.durationSeconds() > 0) {
            analyzeBuildMetrics(repo, event);
        }
        
        // General Real-time push for UI updates so the frontend doesn't have to poll
        messagingTemplate.convertAndSend("/topic/builds", event);
    }

    /**
     * Analyzes recent build history to detect anomalies (performance degradation or flaky pipelines).
     * Saves updated metrics to the database and dispatches WebSocket alerts if necessary.
     */
    private void analyzeBuildMetrics(TrackedRepo repo, BuildEvent event) {
        List<BuildState> last10Builds = buildStateRepository.findTop10ByRepoOrderByRunIdDesc(repo);
        
        // Exclude the current run so it doesn't skew the historical data
        List<BuildState> historicalBuilds = last10Builds.stream()
                .filter(state -> !state.getRunId().equals(event.runId()))
                .toList();

        double avgDuration = calculateAverageDuration(historicalBuilds);
        if (avgDuration == 0) return; // Not enough data

        BuildMetric metric = getOrCreateBuildMetric(repo);
        metric.setAvgDurationLast10(new BigDecimal(avgDuration).setScale(2, RoundingMode.HALF_UP));

        checkPerformanceDegradation(repo, event, avgDuration);
        checkFlakyPipeline(repo, event, historicalBuilds, metric);

        buildMetricRepository.save(metric);
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
            if (state.getDurationSeconds() != null) {
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
    private void checkPerformanceDegradation(TrackedRepo repo, BuildEvent event, double avgDuration) {
        if (event.durationSeconds() > (avgDuration * 1.5)) {
            sendAlert("⚠️ Performance Degradation: " + repo.getRepoName() + " build took " + 
                      event.durationSeconds() + "s (Average is " + String.format("%.2f", avgDuration) + "s)");
        }
    }

    /**
     * Detects if the pipeline is repeatedly failing.
     * Looks at the last 5 builds. If there is more than 1 failure and the current build failed, it alerts.
     */
    private void checkFlakyPipeline(TrackedRepo repo, BuildEvent event, List<BuildState> last10Builds, BuildMetric metric) {
        int failureCount = 0;
        for (BuildState state : last10Builds.stream().limit(5).toList()) {
            if ("failure".equalsIgnoreCase(state.getConclusion())) {
                failureCount++;
            }
        }
        
        metric.setFailureCountLast5(failureCount);

        if ("failure".equalsIgnoreCase(event.conclusion()) && failureCount > 1) {
            sendAlert("🚨 Critical: Pipeline Broken for " + repo.getRepoName() + ". Recent failures: " + failureCount);
        }
    }

    /**
     * Dispatches an anomaly alert down the dedicated WebSocket alerts channel.
     */
    private void sendAlert(String message) {
        messagingTemplate.convertAndSend("/topic/alerts", "{\"message\": \"" + message + "\"}");
    }
}
