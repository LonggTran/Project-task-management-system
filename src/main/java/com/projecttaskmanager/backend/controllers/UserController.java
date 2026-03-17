package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.UserResponse;
import com.projecttaskmanager.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Get current user success")
                .data(userService.getCurrentUser())
                .timestamp(Instant.now())
                .build();
    }
}