package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.request.project.UpdateProjectRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;
import com.projecttaskmanager.backend.services.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project created")
                .data(projectService.createProject(request, authentication.getName()))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                .success(true)
                .message("All projects")
                .data(projectService.getAllProjects(authentication.getName()))
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .data(projectService.getProjectById(id, authentication.getName()))
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .data(projectService.updateProject(id, request, authentication.getName()))
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        projectService.deleteProject(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project deleted")
                .timestamp(Instant.now())
                .build());
    }
}