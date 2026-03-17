package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;
import com.projecttaskmanager.backend.services.ProjectService;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<ProjectResponse> createProject(@RequestBody CreateProjectRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project created")
                .data(projectService.createProject(request))
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> getAllProjects() {

        return ApiResponse.<List<ProjectResponse>>builder()
                .success(true)
                .message("All projects")
                .data(projectService.getAllProjects())
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return ApiResponse.<ProjectResponse>builder()
                .success(true)
                .data(projectService.getProjectById(id))
                .timestamp(Instant.now())
                .build();
    }
}
