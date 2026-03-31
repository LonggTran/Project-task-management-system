package com.projecttaskmanager.backend.services;

import java.time.Duration;

public interface JwtRedisService {
    void blacklistToken(String token, long expiration);
    boolean isTokenBlacklisted(String token);
    void saveRefreshToken(String email, String refreshToken, long expiration);
    boolean isRefreshTokenValid(String email, String refreshToken);
    void deleteRefreshToken(String email);
}
