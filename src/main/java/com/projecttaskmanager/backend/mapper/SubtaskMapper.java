package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.task.SubtaskResponse;
import com.projecttaskmanager.backend.models.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubtaskMapper {

    private final UserMapper userMapper;
    private final TaskStatusMapper taskStatusMapper;

    public SubtaskResponse toResponse(Task task) {

        if (task == null) return null;

        return SubtaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .type(task.getType())
                .dueDate(task.getDueDate())
                .estimatedTime(task.getEstimatedTime())
                .status(taskStatusMapper.toResponse(task.getStatus()))
                .createdBy(userMapper.toResponse(task.getCreatedBy()))
                .parentTaskId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .build();
    }
}