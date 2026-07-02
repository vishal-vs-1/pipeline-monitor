package com.vigilant.vigilant_backend.dto.response;

import java.util.List;

public record RepoBuildsResponse(
    Long repoId,
    String repoName,
    String branch,
    List<BuildStateResponse> builds
) {}
