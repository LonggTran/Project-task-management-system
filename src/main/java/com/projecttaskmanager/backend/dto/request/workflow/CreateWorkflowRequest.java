package com.projecttaskmanager.backend.dto.request.workflow;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateWorkflowRequest {
    private String name;
    private UUID projectId;
}