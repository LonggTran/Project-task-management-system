package com.projecttaskmanager.backend.dto.response.task;

import com.projecttaskmanager.backend.models.enums.TaskStatusCategory;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TaskStatusResponse {
    private UUID id;
    private String name;
    private TaskStatusCategory category;
    private Boolean isDefault;
    private Integer order;
}