package com.projecttaskmanager.backend.dto.response.project;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProjectMemberResponse {
    private UserResponse user;
    private String projectRole;
    private Instant joinedAt;
}
