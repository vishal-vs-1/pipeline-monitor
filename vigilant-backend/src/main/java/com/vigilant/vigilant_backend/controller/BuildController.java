package com.vigilant.vigilant_backend.controller;

import com.vigilant.vigilant_backend.dto.response.BuildStateResponse;
import com.vigilant.vigilant_backend.entity.BuildState;
import com.vigilant.vigilant_backend.service.BuildService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/builds")
@RequiredArgsConstructor
public class BuildController {

    private final BuildService buildService;

    @GetMapping("/recent")
    public Map<String, List<BuildStateResponse>> getRecentBuilds() {
        Map<String, List<BuildState>> recentBuilds = buildService.getRecentBuildsGroupedByRepo();
        
        return recentBuilds.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(BuildStateResponse::fromEntity)
                                .toList()
                ));
    }
}
