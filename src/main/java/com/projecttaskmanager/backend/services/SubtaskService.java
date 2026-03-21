package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.response.task.SubtaskResponse;
import com.projecttaskmanager.backend.dto.response.task.SubtaskTreeResponse;

import java.util.List;
import java.util.UUID;

public interface SubtaskService {
    SubtaskResponse create(UUID parentTaskId, CreateTaskRequest request);
    List<SubtaskResponse> getByParent(UUID parentTaskId);
    void delete(UUID subtaskId);
    SubtaskTreeResponse getTree(UUID taskId);
}