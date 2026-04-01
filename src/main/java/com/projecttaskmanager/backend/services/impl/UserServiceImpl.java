package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
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

    private static final String CACHE_USER_ME = "user:me:";

    @Override
    public UserResponse getCurrentUser(String email) {
        String cacheKey = CACHE_USER_ME + email;
        UserResponse cached = redisService.get(cacheKey, UserResponse.class);

        if (cached != null) return cached;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();

        redisService.set(cacheKey, response, 30, TimeUnit.MINUTES);
        return response;
    }
}