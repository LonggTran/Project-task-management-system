package com.projecttaskmanager.backend.dto.request.sprint;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddTaskToSprintRequest {
    @NotNull(message = "Sprint ID is required")
    private UUID sprintId;

    @NotNull(message = "Task ID is required")
    private UUID taskId;
}