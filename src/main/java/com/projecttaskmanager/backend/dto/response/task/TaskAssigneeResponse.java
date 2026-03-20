package com.projecttaskmanager.backend.dto.response.task;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TaskAssigneeResponse {
    private UUID id;
    private UserResponse user;
    private TaskResponse task;
    private UUID taskId;
}