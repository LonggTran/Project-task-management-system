package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;

import java.util.List;
import java.util.UUID;

public interface TaskStatusService {
    TaskStatusResponse create(CreateTaskStatusRequest request);
    TaskStatusResponse update(UUID id, UpdateTaskStatusRequest request);
    void delete(UUID id);
    TaskStatusResponse getById(UUID id);
    List<TaskStatusResponse> getAll();
}