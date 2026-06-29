package com.vigilant.vigilant_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowRunsResponse(
    @JsonProperty("workflow_runs") List<WorkflowRun> workflowRuns
) {}
