package com.projecttaskmanager.backend.dto.request.project;

import com.projecttaskmanager.backend.models.emuns.ProjectRole;
import lombok.Data;

import java.util.UUID;

@Data
public class AddMemberRequest {
    private UUID userId;
    private ProjectRole roleInProject;
}
