package com.projecttaskmanager.backend.dto.request.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateWorkflowRequest {
    @NotBlank(message = "Workflow name is required")
    @Size(min = 1, max = 200, message = "Workflow name must be between 1 and 200 characters")
    private String name;

    @NotNull(message = "Project ID is required")
    private UUID projectId;
}