package com.vigilant.vigilant_backend.dto.response;

import java.time.LocalDateTime;

public record BuildStateResponse(
    Long id,
    Long repoId,
    Long runId,
    String status,
    String conclusion,
    Integer durationSeconds,
    LocalDateTime updatedAt
) {
    public static BuildStateResponse fromEntity(com.vigilant.vigilant_backend.entity.BuildState state) {
        return new BuildStateResponse(
            state.getId(),
            state.getRepo().getId(),
            state.getRunId(),
            state.getStatus(),
            state.getConclusion(),
            state.getDurationSeconds(),
            state.getUpdatedAt()
        );
    }
}
