package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;
import com.projecttaskmanager.backend.services.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/task-status")
@RequiredArgsConstructor
public class TaskStatusController {

    private final TaskStatusService taskStatusService;

    @PostMapping
    public ApiResponse<TaskStatusResponse> create(@RequestBody CreateTaskStatusRequest request) {
        return ApiResponse.<TaskStatusResponse>builder()
                .success(true)
                .message("Create task status successfully")
                .data(taskStatusService.create(request))
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskStatusResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateTaskStatusRequest request
    ) {
        return ApiResponse.<TaskStatusResponse>builder()
                .success(true)
                .message("Update task status successfully")
                .data(taskStatusService.update(id, request))
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        taskStatusService.delete(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Delete task status successfully")
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskStatusResponse> getById(@PathVariable UUID id) {
        return ApiResponse.<TaskStatusResponse>builder()
                .success(true)
                .data(taskStatusService.getById(id))
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping
    public ApiResponse<List<TaskStatusResponse>> getAll() {
        return ApiResponse.<List<TaskStatusResponse>>builder()
                .success(true)
                .data(taskStatusService.getAll())
                .timestamp(Instant.now())
                .build();
    }
}