package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.task.AssignTaskRequest;
import com.projecttaskmanager.backend.dto.response.task.TaskAssigneeResponse;

import java.util.List;
import java.util.UUID;

public interface TaskAssigneeService {
    TaskAssigneeResponse assignTask(AssignTaskRequest request);
    TaskAssigneeResponse assignToMe(UUID taskId);
    void unassignTask(AssignTaskRequest request);
    List<TaskAssigneeResponse> getAssignees(UUID taskId);
}