package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.services.JwtRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtRedisServiceImpl implements JwtRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_PREFIX = "refresh:";

    @Override
    public void blacklistToken(String token, long expiration) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "blacklisted",
                expiration,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    @Override
    public void saveRefreshToken(String email, String refreshToken, long expiration) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + email,
                refreshToken,
                expiration,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean isRefreshTokenValid(String email, String refreshToken) {
        Object stored = redisTemplate.opsForValue().get(REFRESH_PREFIX + email);
        return stored != null && stored.equals(refreshToken);
    }

    @Override
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);
    }
}