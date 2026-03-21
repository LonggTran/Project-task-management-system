package com.projecttaskmanager.backend.dto.request.workflow;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateWorkflowStepRequest {
    private UUID workflowId;
    private UUID fromStatusId;
    private UUID toStatusId;
    private String requiredPermission;
}