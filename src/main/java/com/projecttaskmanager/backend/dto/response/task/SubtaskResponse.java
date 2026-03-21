package com.projecttaskmanager.backend.dto.response.task;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import com.projecttaskmanager.backend.models.enums.TaskPriority;
import com.projecttaskmanager.backend.models.enums.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class SubtaskResponse {
    private UUID id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskType type;
    private LocalDate dueDate;
    private Integer estimatedTime;
    private TaskStatusResponse status;
    private UserResponse createdBy;
    private UUID parentTaskId;
    private UUID projectId;
}