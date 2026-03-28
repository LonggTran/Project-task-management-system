package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.sprint.AddTaskToSprintRequest;
import com.projecttaskmanager.backend.dto.request.sprint.CreateSprintRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.sprint.SprintResponse;
import com.projecttaskmanager.backend.services.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ApiResponse<?> create(@RequestBody CreateSprintRequest request) {
        return ApiResponse.builder()
                .success(true)
                .data(sprintService.create(request))
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/{id}/start")
    public ApiResponse<?> start(@PathVariable UUID id) {
        return ApiResponse.builder()
                .success(true)
                .data(sprintService.startSprint(id))
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/{id}/close")
    public ApiResponse<?> close(@PathVariable UUID id) {
        return ApiResponse.builder()
                .success(true)
                .data(sprintService.closeSprint(id))
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/tasks")
    public ApiResponse<?> addTask(@RequestBody AddTaskToSprintRequest request) {
        return ApiResponse.builder()
                .success(true)
                .data(sprintService.addTask(request))
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResponse<?> removeTask(@PathVariable UUID id) {
        sprintService.removeTask(id);
        return ApiResponse.builder()
                .success(true)
                .message("Removed")
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<?> getByProject(@PathVariable UUID projectId) {
        return ApiResponse.builder()
                .success(true)
                .data(sprintService.getByProject(projectId))
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}/tasks")
    public ApiResponse<?> getSprintTasks(@PathVariable UUID id) {
        return ApiResponse.builder()
                .success(true)
                .data(sprintService.getSprintTasks(id))
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{sprintId}")
    public ResponseEntity<ApiResponse<SprintResponse>> update(@PathVariable UUID sprintId, @RequestBody CreateSprintRequest request) {
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
                .message("sprint removed from task")
                .timestamp(Instant.now())
                .build());
    }
}