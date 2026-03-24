// controllers/TaskStatusController.java
package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;
import com.projecttaskmanager.backend.services.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/task-status")
@RequiredArgsConstructor
public class TaskStatusController {

    private final TaskStatusService taskStatusService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskStatusResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<TaskStatusResponse>>builder()
                .success(true)
                .data(taskStatusService.getAll())
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskStatusResponse>>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.<List<TaskStatusResponse>>builder()
                .success(true)
                .data(taskStatusService.getByProject(projectId))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/project/{projectId}/default")
    public ResponseEntity<ApiResponse<TaskStatusResponse>> getDefaultStatus(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.<TaskStatusResponse>builder()
                .success(true)
                .data(taskStatusService.getDefaultStatus(projectId))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<TaskStatusResponse>> create(
            @PathVariable UUID projectId,
            @RequestBody CreateTaskStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<TaskStatusResponse>builder()
                .success(true)
                .data(taskStatusService.create(projectId, request))
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskStatusResponse>> update(
            @PathVariable UUID id,
            @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<TaskStatusResponse>builder()
                .success(true)
                .data(taskStatusService.update(id, request))
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        taskStatusService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Status deleted")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskStatusResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<TaskStatusResponse>builder()
                .success(true)
                .data(taskStatusService.getById(id))
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/project/{projectId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderStatuses(
            @PathVariable UUID projectId,
            @RequestBody Map<String, List<Map<String, Object>>> request) {
        List<Map<String, Object>> statuses = request.get("statuses");
        taskStatusService.reorderStatuses(projectId, statuses);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Status order updated")
                .timestamp(Instant.now())
                .build());
    }
}