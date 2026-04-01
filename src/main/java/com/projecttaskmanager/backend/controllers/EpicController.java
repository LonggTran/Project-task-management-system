package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.epic.CreateEpicRequest;
import com.projecttaskmanager.backend.dto.request.epic.UpdateEpicRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.epic.EpicResponse;
import com.projecttaskmanager.backend.services.EpicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/epics")
@RequiredArgsConstructor
public class EpicController {

    private final EpicService epicService;

    @PostMapping
    public ResponseEntity<ApiResponse<EpicResponse>> create(
            @Valid @RequestBody CreateEpicRequest request,
            Authentication authentication // Thêm ở đây
    ) {
        return ResponseEntity.ok(ApiResponse.<EpicResponse>builder()
                .success(true)
                .data(epicService.create(request, authentication.getName()))
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EpicResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEpicRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.<EpicResponse>builder()
                .success(true)
                .data(epicService.update(id, request, authentication.getName()))
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        epicService.delete(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Epic deleted")
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EpicResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<EpicResponse>builder()
                .success(true)
                .data(epicService.getById(id))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<EpicResponse>>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.<List<EpicResponse>>builder()
                .success(true)
                .data(epicService.getAllByProject(projectId))
                .timestamp(Instant.now())
                .build());
    }
}