package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.task.SubtaskResponse;
import com.projecttaskmanager.backend.dto.response.task.SubtaskTreeResponse;
import com.projecttaskmanager.backend.services.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;

    @PostMapping("/{parentId}")
    public ResponseEntity<ApiResponse<SubtaskResponse>> create(
            @PathVariable UUID parentId,
            @RequestBody CreateTaskRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<SubtaskResponse>builder()
                .success(true)
                .data(subtaskService.create(parentId, request))
                .message("add subtask success")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{parentId}")
    public ResponseEntity<ApiResponse<List<SubtaskResponse>>> getByParent(
            @PathVariable UUID parentId
    ) {
        return ResponseEntity.ok(ApiResponse.<List<SubtaskResponse>>builder()
                .success(true)
                .data(subtaskService.getByParent(parentId))
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {

        subtaskService.delete(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Subtask deleted")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/tree/{taskId}")
    public ResponseEntity<ApiResponse<SubtaskTreeResponse>> getTree(@PathVariable UUID taskId) {

        return ResponseEntity.ok(ApiResponse.<SubtaskTreeResponse>builder()
                .success(true)
                .data(subtaskService.getTree(taskId))
                .timestamp(Instant.now())
                .build());
    }
}