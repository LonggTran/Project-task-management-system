package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.response.UserResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.UserMapper;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.UserService;
import com.projecttaskmanager.backend.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final UserMapper userMapper;

    private static final long USER_CACHE_TTL = 30;

    @Override
    public UserResponse getCurrentUser(String email) {
        String cacheKey = CacheKey.userMe(email);

        UserResponse cached = redisService.get(cacheKey, UserResponse.class);
        if (cached != null) return cached;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserResponse response = userMapper.toResponse(user);

        redisService.set(cacheKey, response, USER_CACHE_TTL, TimeUnit.MINUTES);

        return response;
    }
}