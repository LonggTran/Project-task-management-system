package com.projecttaskmanager.backend.dto.request.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RegisterRequest {
    private String email;

    private String password;

    private String fullName;
}