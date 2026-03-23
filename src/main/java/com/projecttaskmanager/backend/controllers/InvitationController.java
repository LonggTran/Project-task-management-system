package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.services.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final ProjectMemberService projectMemberService;

    @PostMapping("/accept")
    public ApiResponse<Void> accept(@RequestParam String token) {

        projectMemberService.acceptInvitation(token);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Joined project successfully")
                .timestamp(Instant.now())
                .build();
    }
}
