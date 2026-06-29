package com.vigilant.vigilant_backend.kafka.service;

import com.vigilant.vigilant_backend.kafka.BuildEvent;

public interface AnomalyDetectorService {
    void consumeBuildEvent(BuildEvent event);
}
