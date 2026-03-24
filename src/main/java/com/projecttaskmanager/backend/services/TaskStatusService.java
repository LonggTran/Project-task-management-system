// services/TaskStatusService.java
package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TaskStatusService {
    TaskStatusResponse create(UUID projectId, CreateTaskStatusRequest request);
    TaskStatusResponse update(UUID id, UpdateTaskStatusRequest request);
    void delete(UUID id);
    TaskStatusResponse getById(UUID id);
    List<TaskStatusResponse> getByProject(UUID projectId);
    List<TaskStatusResponse> getAll();
    TaskStatusResponse getDefaultStatus(UUID projectId);
    void reorderStatuses(UUID projectId, List<Map<String, Object>> statuses);
}