package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.task.TaskResponse;
import com.projecttaskmanager.backend.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(@RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.create(request);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable UUID id,
                                                            @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.update(id, request);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Task deleted")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(@PathVariable UUID id) {
        TaskResponse response = taskService.getById(id);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllByProject(@PathVariable UUID projectId) {
        List<TaskResponse> response = taskService.getAllByProject(projectId);
        return ResponseEntity.ok(ApiResponse.<List<TaskResponse>>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }
}