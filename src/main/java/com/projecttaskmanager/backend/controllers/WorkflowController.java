package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowRequest;
import com.projecttaskmanager.backend.dto.request.workflow.CreateWorkflowStepRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.services.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public ApiResponse<?> createWorkflow(@RequestBody CreateWorkflowRequest request) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.createWorkflow(request))
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/steps")
    public ApiResponse<?> createStep(@RequestBody CreateWorkflowStepRequest request) {
        return ApiResponse.builder()
                .success(true)
                .data(workflowService.createStep(request))
                .timestamp(Instant.now())
                .build();
    }
}