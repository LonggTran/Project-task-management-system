package com.projecttaskmanager.backend.dto.request.task;

import com.projecttaskmanager.backend.models.enums.TaskPriority;
import com.projecttaskmanager.backend.models.enums.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 500, message = "Task title must be between 1 and 500 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotNull(message = "Status ID is required")
    private UUID statusId;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Task type is required")
    private TaskType type;

    @Future(message = "Due date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @Min(value = 0, message = "Estimated time must be greater than or equal to 0")
    private Integer estimatedTime;
}