package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.project.AddMemberRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
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
    public ApiResponse<Void> addMember(@PathVariable UUID projectId, @RequestBody AddMemberRequest request) {
        projectMemberService.addMember(projectId, request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Member added")
                .timestamp(Instant.now())
                .build();
    }
}