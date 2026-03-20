package com.projecttaskmanager.backend.dto.request.task;

import lombok.Data;
import java.util.UUID;

@Data
public class AssignTaskRequest {
    private UUID taskId;
    private UUID userId;
}