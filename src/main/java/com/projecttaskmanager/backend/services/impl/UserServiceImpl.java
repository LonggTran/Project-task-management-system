package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.response.UserResponse;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.UserMapper;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.services.RedisService;
import com.projecttaskmanager.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RedisService redisService;
    private final UserMapper userMapper;
    private final EntityHelper entityHelper;

    private static final long USER_CACHE_TTL = 30;

    @Override
    public UserResponse getCurrentUser(String email) {

        String cacheKey = CacheKey.userMe(email);

        return redisService.getOrLoad(
                cacheKey,
                UserResponse.class,
                () -> {
                    User user = entityHelper.getUserOrThrow(email);
                    return userMapper.toResponse(user);
                },
                USER_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }
}