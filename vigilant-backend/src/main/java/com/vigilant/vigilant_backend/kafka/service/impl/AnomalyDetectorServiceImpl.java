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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnomalyDetectorServiceImpl implements AnomalyDetectorService {

    private final BuildStateRepository buildStateRepository;
    private final BuildMetricRepository buildMetricRepository;
    private final TrackedRepoRepository trackedRepoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Kafka Listener that consumes batches of BuildEvents emitted by the BuildPoller.
     * It runs anomaly detection algorithms and broadcasts the event to the UI via WebSockets.
     */
    @Override
    @KafkaListener(topics = "build-events", groupId = "vigilant-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumeBuildEvents(List<BuildEvent> events) {
        if (events == null || events.isEmpty()) return;

        // Grouping events chronologically if there are multiple for the same runId doesn't strictly matter here 
        // as long as we process them. We'll iterate in the order received.
        Set<Long> repoIds = events.stream().map(BuildEvent::repoId).collect(Collectors.toSet());
        Map<Long, TrackedRepo> repos = trackedRepoRepository.findAllById(repoIds).stream()
                .collect(Collectors.toMap(TrackedRepo::getId, r -> r));

        Set<Long> runIds = events.stream().map(BuildEvent::runId).collect(Collectors.toSet());
        Map<Long, BuildState> existingStates = buildStateRepository.findByRunIdIn(runIds).stream()
                .collect(Collectors.toMap(BuildState::getRunId, s -> s));

        List<BuildState> statesToSave = new ArrayList<>();
        Map<Long, BuildMetric> metricsToSave = new HashMap<>();

        for (BuildEvent event : events) {
            TrackedRepo repo = repos.get(event.repoId());
            if (repo == null) continue;

            BuildState existing = existingStates.get(event.runId());
            
            // Idempotency: skip if we have this exact event state processed already
            if (existing != null && 
                String.valueOf(existing.getStatus()).equals(String.valueOf(event.status())) &&
                String.valueOf(existing.getConclusion()).equals(String.valueOf(event.conclusion())) &&
                Objects.equals(existing.getDurationSeconds(), event.durationSeconds())) {
                continue;
            }

            boolean isAnomaly = false;
            if (event.durationSeconds() != null && event.durationSeconds() > 0) {
                // To avoid N+1, ideally we'd batch fetch historical data, but window sizes vary. 
                // We'll leave the historical fetch per anomaly check for now.
                isAnomaly = analyzeBuildMetrics(repo, event, metricsToSave);
            }
            
            BuildEvent finalEvent = new BuildEvent(
                event.repoId(), event.repoName(), event.runId(), 
                event.status(), event.conclusion(), event.durationSeconds(), isAnomaly
            );
            
            // User-scoped broadcast
            String userEmail = repo.getUser().getEmail();
            messagingTemplate.convertAndSendToUser(userEmail, "/queue/builds", finalEvent);
            if (isAnomaly) {
                messagingTemplate.convertAndSendToUser(userEmail, "/queue/alerts", finalEvent);
            }
            
            if (existing == null) {
                existing = new BuildState();
                existing.setRunId(event.runId());
                existing.setRepo(repo);
                existingStates.put(event.runId(), existing); // Cache for subsequent events in batch
            }
            
            existing.setStatus(event.status());
            existing.setConclusion(event.conclusion());
            existing.setDurationSeconds(event.durationSeconds());
            if (isAnomaly) {
                existing.setIsAnomaly(true);
            }
            
            statesToSave.add(existing);
        }

        if (!statesToSave.isEmpty()) {
            buildStateRepository.saveAll(statesToSave);
        }
        if (!metricsToSave.isEmpty()) {
            buildMetricRepository.saveAll(metricsToSave.values());
        }
    }

    private boolean analyzeBuildMetrics(TrackedRepo repo, BuildEvent event, Map<Long, BuildMetric> metricsToSave) {
        int limit = repo.getAnomalyWindowSize();
        List<BuildState> lastBuilds = buildStateRepository.findByRepoOrderByRunIdDesc(repo, org.springframework.data.domain.PageRequest.of(0, limit + 1)).getContent();
        
        List<BuildState> historicalBuilds = lastBuilds.stream()
                .filter(state -> !state.getRunId().equals(event.runId()))
                .limit(limit)
                .toList();

        double avgDuration = calculateAverageDuration(historicalBuilds);
        if (avgDuration == 0) return false;

        BuildMetric metric = metricsToSave.getOrDefault(repo.getId(), getOrCreateBuildMetric(repo));
        metric.setAvgDurationLast10(new BigDecimal(avgDuration).setScale(2, RoundingMode.HALF_UP));

        boolean isPerfAnomaly = checkPerformanceDegradation(repo, event, avgDuration);
        boolean isFlakyAnomaly = checkFlakyPipeline(repo, event, historicalBuilds, metric);
        
        metricsToSave.put(repo.getId(), metric);
        
        return isPerfAnomaly || isFlakyAnomaly;
    }

    private BuildMetric getOrCreateBuildMetric(TrackedRepo repo) {
        BuildMetric metric = buildMetricRepository.findById(repo.getId()).orElse(new BuildMetric());
        metric.setRepoId(repo.getId());
        return metric;
    }

    private double calculateAverageDuration(List<BuildState> builds) {
        double sumDuration = 0;
        int count = 0;
        for (BuildState state : builds) {
            if (state.getDurationSeconds() != null && "completed".equalsIgnoreCase(state.getStatus())) {
                sumDuration += state.getDurationSeconds();
                count++;
            }
        }
        return count > 0 ? sumDuration / count : 0;
    }

    private boolean checkPerformanceDegradation(TrackedRepo repo, BuildEvent event, double avgDuration) {
        double multiplier = repo.getAnomalyMultiplier();
        return event.durationSeconds() > (avgDuration * multiplier);
    }

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
