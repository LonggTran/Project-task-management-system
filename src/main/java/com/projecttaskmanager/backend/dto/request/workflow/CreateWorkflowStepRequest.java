package com.projecttaskmanager.backend.dto.request.workflow;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateWorkflowStepRequest {
    @NotNull(message = "Workflow ID is required")
    private UUID workflowId;

    @NotNull(message = "From status ID is required")
    private UUID fromStatusId;

    @NotNull(message = "To status ID is required")
    private UUID toStatusId;

    private String requiredPermission;
}