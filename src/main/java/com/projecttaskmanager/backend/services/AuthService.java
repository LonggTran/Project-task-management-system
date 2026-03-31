package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.auth.LoginRequest;
import com.projecttaskmanager.backend.dto.request.auth.RegisterRequest;
import com.projecttaskmanager.backend.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse loginWithGoogle(String idToken);
    void sendOtp(RegisterRequest request);
    AuthResponse verifyOtp(String email, String otp);
    void logout(String accessToken);
    AuthResponse refreshToken(String refreshToken);
}