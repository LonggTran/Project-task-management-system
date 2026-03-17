package com.projecttaskmanager.backend.dto.response.project;

import com.projecttaskmanager.backend.models.emuns.ProjectRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProjectMemberResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private ProjectRole roleInProject;
    private Instant joinedAt;
}
