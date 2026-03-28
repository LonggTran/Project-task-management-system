package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.auth.GoogleLoginRequest;
import com.projecttaskmanager.backend.dto.request.auth.LoginRequest;
import com.projecttaskmanager.backend.dto.request.auth.RegisterRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.auth.AuthResponse;
import com.projecttaskmanager.backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Register success")
                .data(authService.register(request))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login success")
                .data(authService.login(request))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Google login success")
                .data(authService.loginWithGoogle(request.getIdToken()))
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody RegisterRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("OTP sent")
                .timestamp(Instant.now())
                .build());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @RequestParam @jakarta.validation.constraints.Email String email,
            @RequestParam @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{6}$") String otp) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .data(authService.verifyOtp(email, otp))
                .timestamp(Instant.now())
                .build());
    }
}