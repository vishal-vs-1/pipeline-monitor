package com.vigilant.vigilant_backend.kafka.service;

import com.vigilant.vigilant_backend.kafka.BuildEvent;

import java.util.List;

public interface AnomalyDetectorService {
    void consumeBuildEvents(List<BuildEvent> events);
}
