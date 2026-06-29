package com.vigilant.vigilant_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.vigilant.vigilant_backend.entity.TrackedRepo;

public record AddRepoRequest(
    @NotBlank(message = "{validation.repo.name.required}")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$", message = "{validation.repo.name.pattern}")
    String repoName,

    @NotBlank(message = "{validation.github.token.required}")
    String githubToken,

    @NotBlank(message = "{validation.branch.required}")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "{validation.branch.pattern}")
    String branch
) {
    public TrackedRepo toEntity() {
        TrackedRepo repo = new TrackedRepo();
        repo.setRepoName(this.repoName());
        repo.setGithubToken(this.githubToken());
        repo.setBranch(this.branch());
        return repo;
    }
}
