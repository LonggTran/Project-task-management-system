// controllers/WorkflowController.java
package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowRequest;
import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowStepRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.services.WorkflowService;
import lombok.RequiredArgsConstructor;
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

    // Tạo workflow mới
    @PostMapping
    public ApiResponse<?> createWorkflow(@RequestBody CreateWorkflowRequest request) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.createWorkflow(request))
                .timestamp(Instant.now())
                .build();
    }

    // Cập nhật workflow
    @PutMapping("/{workflowId}")
    public ApiResponse<?> updateWorkflow(
            @PathVariable UUID workflowId,
            @RequestBody Map<String, String> request) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.updateWorkflow(workflowId, request.get("name")))
                .timestamp(Instant.now())
                .build();
    }

    // Lấy workflow theo project
    @GetMapping("/project/{projectId}")
    public ApiResponse<?> getWorkflowByProject(@PathVariable UUID projectId) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.getWorkflowByProject(projectId))
                .timestamp(Instant.now())
                .build();
    }

    // Lấy steps của workflow
    @GetMapping("/{workflowId}/steps")
    public ApiResponse<?> getWorkflowSteps(@PathVariable UUID workflowId) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.getWorkflowSteps(workflowId))
                .timestamp(Instant.now())
                .build();
    }

    // Tạo step mới
    @PostMapping("/steps")
    public ApiResponse<?> createStep(@RequestBody CreateWorkflowStepRequest request) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.createStep(request))
                .timestamp(Instant.now())
                .build();
    }

    // Xóa step
    @DeleteMapping("/steps/{stepId}")
    public ApiResponse<?> deleteStep(@PathVariable UUID stepId) {
        workflowService.deleteStep(stepId);
        return ApiResponse.builder()
                .success(true)
                .message("Step deleted successfully")
                .timestamp(Instant.now())
                .build();
    }

    // Kiểm tra workflow tồn tại
    @GetMapping("/project/{projectId}/exists")
    public ApiResponse<?> hasWorkflow(@PathVariable UUID projectId) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.hasWorkflow(projectId))
                .timestamp(Instant.now())
                .build();
    }

    // Setup workflow mặc định
    @PostMapping("/project/{projectId}/setup-default")
    public ApiResponse<?> setupDefaultWorkflow(@PathVariable UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        if (workflowService.hasWorkflow(projectId)) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Workflow already exists for this project")
                    .timestamp(Instant.now())
                    .build();
        }

        workflowService.createDefaultWorkflow(project);

        return ApiResponse.builder()
                .success(true)
                .message("Default workflow created successfully")
                .timestamp(Instant.now())
                .build();
    }
}