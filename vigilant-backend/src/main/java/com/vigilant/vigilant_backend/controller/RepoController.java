package com.vigilant.vigilant_backend.controller;

import com.vigilant.vigilant_backend.dto.request.AddRepoRequest;
import com.vigilant.vigilant_backend.dto.response.TrackedRepoResponse;
import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.entity.User;
import com.vigilant.vigilant_backend.repository.UserRepository;
import com.vigilant.vigilant_backend.service.RepoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import com.vigilant.vigilant_backend.dto.request.UpdateRepoRequest;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoService repoService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<TrackedRepoResponse> getRepos(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return repoService.getAllRepos(user).stream()
                .map(TrackedRepoResponse::fromEntity)
                .toList();
    }

    @PostMapping
    public TrackedRepoResponse addRepo(@Valid @RequestBody AddRepoRequest request, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        TrackedRepo repo = request.toEntity();
        TrackedRepo savedRepo = repoService.addRepo(repo, user);
        return TrackedRepoResponse.fromEntity(savedRepo);
    }

    @PutMapping("/{id}")
    public TrackedRepoResponse updateRepo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRepoRequest request,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        TrackedRepo updatedRepo = repoService.updateRepo(id, request, user);
        return TrackedRepoResponse.fromEntity(updatedRepo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepo(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        repoService.deleteRepo(id, user);
        return ResponseEntity.noContent().build();
    }
}
