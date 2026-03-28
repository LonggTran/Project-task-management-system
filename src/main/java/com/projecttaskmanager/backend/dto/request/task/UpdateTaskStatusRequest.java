package com.projecttaskmanager.backend.dto.request.task;

import com.projecttaskmanager.backend.models.enums.TaskStatusCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {
    @Size(min = 1, max = 100, message = "Status name must be between 1 and 100 characters")
    private String name;

    private TaskStatusCategory category;

    private Boolean isDefault;

    @Min(value = 0, message = "Order must be greater than or equal to 0")
    private Integer order;
}