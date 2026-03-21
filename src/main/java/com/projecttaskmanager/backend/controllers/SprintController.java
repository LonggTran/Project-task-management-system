package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.sprint.AddTaskToSprintRequest;
import com.projecttaskmanager.backend.dto.request.sprint.CreateSprintRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.services.SprintService;
import lombok.RequiredArgsConstructor;
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
}