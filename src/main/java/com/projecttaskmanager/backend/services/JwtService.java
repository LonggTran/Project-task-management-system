package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.models.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String extractEmail(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    long getRemainingTime(String token);
}