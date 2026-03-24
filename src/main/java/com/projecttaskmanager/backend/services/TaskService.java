package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskRequest;
import com.projecttaskmanager.backend.dto.response.label.LabelResponse;
import com.projecttaskmanager.backend.dto.response.task.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse create(CreateTaskRequest request);
    TaskResponse update(UUID taskId, UpdateTaskRequest request);
    void delete(UUID taskId);
    TaskResponse getById(UUID taskId);
    List<TaskResponse> getAllByProject(UUID projectId);

    List<LabelResponse> getTaskLabels(UUID taskId);
    void addLabelToTask(UUID taskId, UUID labelId);
    void removeLabelFromTask(UUID taskId, UUID labelId);
}