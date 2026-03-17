package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.response.UserResponse;

public interface UserService {
    UserResponse getCurrentUser();
}