package com.vigilant.vigilant_backend.controller;

import com.vigilant.vigilant_backend.dto.request.AddRepoRequest;
import com.vigilant.vigilant_backend.dto.response.TrackedRepoResponse;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.service.RepoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoService repoService;

    @GetMapping
    public List<TrackedRepoResponse> getRepos() {
        return repoService.getAllRepos().stream()
                .map(TrackedRepoResponse::fromEntity)
                .toList();
    }

    @PostMapping
    public TrackedRepoResponse addRepo(@Valid @RequestBody AddRepoRequest request) {
        TrackedRepo repo = request.toEntity();
        TrackedRepo savedRepo = repoService.addRepo(repo);
        return TrackedRepoResponse.fromEntity(savedRepo);
    }
}
