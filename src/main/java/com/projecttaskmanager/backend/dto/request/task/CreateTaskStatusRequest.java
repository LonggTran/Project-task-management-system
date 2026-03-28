package com.projecttaskmanager.backend.dto.request.task;

import com.projecttaskmanager.backend.models.enums.TaskStatusCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTaskStatusRequest {
    @NotBlank(message = "Status name is required")
    @Size(min = 1, max = 100, message = "Status name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Status category is required")
    private TaskStatusCategory category;

    private Boolean isDefault;

    @Min(value = 0, message = "Sort order must be greater than or equal to 0")
    private Integer sort;
}