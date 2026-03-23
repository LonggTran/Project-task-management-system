package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.task.TaskResponse;
import com.projecttaskmanager.backend.models.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final UserMapper userMapper;
    private final TaskStatusMapper taskStatusMapper;

    public TaskResponse toResponse(Task task) {
        if (task == null) return null;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(taskStatusMapper.toResponse(task.getStatus()))
                .priority(task.getPriority())
                .type(task.getType())
                .dueDate(task.getDueDate())
                .estimatedTime(task.getEstimatedTime())
                .createdBy(userMapper.toResponse(task.getCreatedBy()))
                .epicId(task.getEpic() != null ? task.getEpic().getId() : null)  // THÊM DÒNG NÀY
                .parentTaskId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                .build();
    }
}