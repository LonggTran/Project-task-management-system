package com.projecttaskmanager.backend.dto.request.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
}