package com.projecttaskmanager.backend.dto.request.task;

import com.projecttaskmanager.backend.models.enums.TaskPriority;
import com.projecttaskmanager.backend.models.enums.TaskType;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private UUID statusId;
    private TaskPriority priority;
    private TaskType type;
    private LocalDate dueDate;
    private Integer estimatedTime;
    private UUID epicId;
    private UUID parentTaskId;
}