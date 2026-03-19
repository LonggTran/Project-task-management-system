package com.projecttaskmanager.backend.dto.request.project;

import lombok.Data;

import java.util.UUID;

@Data
public class AddMemberRequest {
    private UUID userId;
    private String projectRole;
}
