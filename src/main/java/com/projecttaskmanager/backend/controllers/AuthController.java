package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.auth.GoogleLoginRequest;
import com.projecttaskmanager.backend.dto.request.auth.LoginRequest;
import com.projecttaskmanager.backend.dto.request.auth.RegisterRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.auth.AuthResponse;
import com.projecttaskmanager.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Register success")
                .data(authService.register(request))
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login success")
                .data(authService.login(request))
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> loginWithGoogle(
            @RequestBody GoogleLoginRequest request
    ) {
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Google login success")
                .data(authService.loginWithGoogle(request.getIdToken()))
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/send-otp")
    public ApiResponse<Void> sendOtp(@RequestBody RegisterRequest request) {
        authService.sendOtp(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("OTP sent")
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<AuthResponse> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .data(authService.verifyOtp(email, otp))
                .build();
    }
}