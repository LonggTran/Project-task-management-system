package com.projecttaskmanager.backend.dto.request.sprint;

import lombok.Data;

import java.util.UUID;

@Data
public class AddTaskToSprintRequest {
    private UUID sprintId;
    private UUID taskId;
}