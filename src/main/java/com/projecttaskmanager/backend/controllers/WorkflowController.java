package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowRequest;
import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowStepRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.services.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final ProjectRepository projectRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(workflowService.createWorkflow(request))
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{workflowId}")
    public ResponseEntity<ApiResponse<?>> updateWorkflow(
            @PathVariable UUID workflowId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(workflowService.updateWorkflow(workflowId, request.get("name")))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<?>> getWorkflowByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(workflowService.getWorkflowByProject(projectId))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{workflowId}/steps")
    public ResponseEntity<ApiResponse<?>> getWorkflowSteps(@PathVariable UUID workflowId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(workflowService.getWorkflowSteps(workflowId))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/steps")
    public ResponseEntity<ApiResponse<?>> createStep(@Valid @RequestBody CreateWorkflowStepRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(workflowService.createStep(request))
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<ApiResponse<?>> deleteStep(@PathVariable UUID stepId) {
        workflowService.deleteStep(stepId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Step deleted successfully")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/project/{projectId}/exists")
    public ResponseEntity<ApiResponse<?>> hasWorkflow(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(workflowService.hasWorkflow(projectId))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/project/{projectId}/setup-default")
    public ResponseEntity<ApiResponse<?>> setupDefaultWorkflow(@PathVariable UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        if (workflowService.hasWorkflow(projectId)) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Workflow already exists for this project")
                    .timestamp(Instant.now())
                    .build());
        }

        workflowService.createDefaultWorkflow(project);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Default workflow created successfully")
                .timestamp(Instant.now())
                .build());
    }
}