package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.services.ProjectMemberService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final ProjectMemberService projectMemberService;

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> accept(
            @RequestParam @NotBlank(message = "Token is required") String token) {
        projectMemberService.acceptInvitation(token);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Joined project successfully")
                .timestamp(Instant.now())
                .build());
    }
}