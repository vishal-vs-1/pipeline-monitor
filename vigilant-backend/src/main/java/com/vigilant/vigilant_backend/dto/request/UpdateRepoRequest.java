package com.vigilant.vigilant_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record UpdateRepoRequest(
        @NotBlank(message = "{validation.repo.name.required}") @Pattern(regexp = "^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$", message = "{validation.repo.name.pattern}") String repoName,

        String githubToken, // Optional during update

        @NotBlank(message = "{validation.branch.required}") @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "{validation.branch.pattern}") String branch,

        @Min(value = 1, message = "{validation.anomaly.multiplier.min}") @Max(value = 5, message = "{validation.anomaly.multiplier.max}") Double anomalyMultiplier,

        @Min(value = 5, message = "{validation.anomaly.window.min}") @Max(value = 20, message = "{validation.anomaly.window.max}") Integer anomalyWindowSize,

        @NotNull Boolean isActive) {
}
