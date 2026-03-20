package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.task.TaskAssigneeResponse;
import com.projecttaskmanager.backend.models.TaskAssignee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAssigneeMapper {

    private final UserMapper userMapper;
    private final TaskMapper taskMapper;

    public TaskAssigneeResponse toResponse(TaskAssignee assignee) {
        return TaskAssigneeResponse.builder()
                .id(assignee.getId())
                .task(taskMapper.toResponse(assignee.getTask()))
                .taskId(assignee.getTask().getId())
                .user(userMapper.toResponse(assignee.getUser()))
                .build();
    }
}