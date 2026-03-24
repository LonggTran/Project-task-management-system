package com.projecttaskmanager.backend.dto.request.auth;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}