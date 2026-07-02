package com.vigilant.vigilant_backend.controller;

import com.vigilant.vigilant_backend.dto.response.BuildStateResponse;
import com.vigilant.vigilant_backend.dto.response.RepoBuildsResponse;
import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.service.BuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/builds")
@RequiredArgsConstructor
public class BuildController {

    private final BuildService buildService;

    @GetMapping("/recent")
    public List<RepoBuildsResponse> getRecentBuilds() {
        return buildService.getRecentBuildsGroupedByRepo();
    }

    @GetMapping("/repo/{repoId}")
    public Page<BuildStateResponse> getRepoBuilds(
            @PathVariable Long repoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<BuildState> buildsPage = buildService.getBuildsForRepo(repoId, PageRequest.of(page, size));
        return buildsPage.map(BuildStateResponse::fromEntity);
    }
}
