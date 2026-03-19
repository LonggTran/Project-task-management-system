package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.project.AddMemberRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.project.ProjectMemberResponse;
import com.projecttaskmanager.backend.services.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ApiResponse<ProjectMemberResponse> addMember(@PathVariable UUID projectId, @RequestBody AddMemberRequest request) {
        return ApiResponse.<ProjectMemberResponse>builder()
                .success(true)
                .message("Member added")
                .data(projectMemberService.addMember(projectId, request))
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> removeMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        projectMemberService.removeMember(projectId, userId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Member removed")
                .timestamp(Instant.now())
                .build();
    }
}