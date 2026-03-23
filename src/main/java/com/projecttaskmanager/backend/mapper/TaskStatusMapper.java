// mapper/TaskStatusMapper.java
package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;
import com.projecttaskmanager.backend.models.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusMapper {

    public TaskStatusResponse toResponse(TaskStatus status) {
        if (status == null) return null;

        return TaskStatusResponse.builder()
                .id(status.getId())
                .name(status.getName())
                .category(status.getCategory())
                .isDefault(status.getIsDefault())
                .order(status.getSort())
                .build();
    }
}