package com.projecttaskmanager.backend.dto.response.workflow;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WorkflowStepResponse {
    private UUID id;
    private UUID workflowId;
    private String fromStatus;
    private String toStatus;
    private String requiredPermission;
}