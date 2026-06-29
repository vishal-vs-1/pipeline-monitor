package com.vigilant.vigilant_backend.dto.response;

import com.vigilant.vigilant_backend.entity.TrackedRepo;

public record TrackedRepoResponse(
    Long id,
    String repoName,
    String branch,
    Boolean isActive
) {
    public static TrackedRepoResponse fromEntity(TrackedRepo repo) {
        return new TrackedRepoResponse(
            repo.getId(),
            repo.getRepoName(),
            repo.getBranch(),
            repo.getIsActive()
        );
    }
}
