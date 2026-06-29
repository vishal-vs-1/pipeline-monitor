package com.vigilant.vigilant_backend.kafka;

public record BuildEvent(
    Long repoId,
    String repoName,
    Long runId,
    String status,
    String conclusion,
    Integer durationSeconds
) {}
