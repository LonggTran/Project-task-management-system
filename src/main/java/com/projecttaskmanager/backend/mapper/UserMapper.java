package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import com.projecttaskmanager.backend.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}