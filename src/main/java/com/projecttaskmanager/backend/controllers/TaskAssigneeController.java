package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.task.AssignTaskRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.task.TaskAssigneeResponse;
import com.projecttaskmanager.backend.services.TaskAssigneeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task-assignees")
@RequiredArgsConstructor
public class TaskAssigneeController {

    private final TaskAssigneeService service;

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<TaskAssigneeResponse>> assign(@Valid @RequestBody AssignTaskRequest request) {
        TaskAssigneeResponse response = service.assignTask(request);
        return ResponseEntity.ok(ApiResponse.<TaskAssigneeResponse>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/assign/me/{taskId}")
    public ResponseEntity<ApiResponse<TaskAssigneeResponse>> assignToMe(@PathVariable UUID taskId) {
        TaskAssigneeResponse response = service.assignToMe(taskId);
        return ResponseEntity.ok(ApiResponse.<TaskAssigneeResponse>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/unassign")
    public ResponseEntity<ApiResponse<Void>> unassign(@Valid @RequestBody AssignTaskRequest request) {
        service.unassignTask(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Task unassigned")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<List<TaskAssigneeResponse>>> getAssignees(@PathVariable UUID taskId) {
        List<TaskAssigneeResponse> response = service.getAssignees(taskId);
        return ResponseEntity.ok(ApiResponse.<List<TaskAssigneeResponse>>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }
}