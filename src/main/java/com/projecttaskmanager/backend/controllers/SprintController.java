package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.sprint.AddTaskToSprintRequest;
import com.projecttaskmanager.backend.dto.request.sprint.CreateSprintRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.sprint.SprintResponse;
import com.projecttaskmanager.backend.dto.response.sprint.SprintTaskResponse;
import com.projecttaskmanager.backend.services.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<ApiResponse<SprintResponse>> create(@Valid @RequestBody CreateSprintRequest request) {
        return ResponseEntity.ok(ApiResponse.<SprintResponse>builder()
                .success(true)
                .data(sprintService.create(request))
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{sprintId}")
    public ResponseEntity<ApiResponse<SprintResponse>> update(
            @PathVariable UUID sprintId,
            @Valid @RequestBody CreateSprintRequest request) {
        SprintResponse response = sprintService.update(sprintId, request);
        return ResponseEntity.ok(ApiResponse.<SprintResponse>builder()
                .success(true)
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{sprintId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID sprintId) {
        sprintService.delete(sprintId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Sprint deleted")
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<SprintResponse>> start(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<SprintResponse>builder()
                .success(true)
                .data(sprintService.startSprint(id))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<SprintResponse>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<SprintResponse>builder()
                .success(true)
                .data(sprintService.closeSprint(id))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<SprintTaskResponse>> addTask(@Valid @RequestBody AddTaskToSprintRequest request) {
        return ResponseEntity.ok(ApiResponse.<SprintTaskResponse>builder()
                .success(true)
                .data(sprintService.addTask(request))
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<Void>> removeTask(@PathVariable UUID id) {
        sprintService.removeTask(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Task removed from sprint")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.<List<SprintResponse>>builder()
                .success(true)
                .data(sprintService.getByProject(projectId))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<List<SprintTaskResponse>>> getSprintTasks(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<List<SprintTaskResponse>>builder()
                .success(true)
                .data(sprintService.getSprintTasks(id))
                .timestamp(Instant.now())
                .build());
    }
}