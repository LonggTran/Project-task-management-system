package com.projecttaskmanager.backend.dto.request.task;

import com.projecttaskmanager.backend.models.enums.TaskStatusCategory;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {
    private String name;
    private TaskStatusCategory category;
    private Boolean isDefault;
    private Integer order;
}