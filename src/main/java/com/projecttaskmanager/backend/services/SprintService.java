package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.sprint.AddTaskToSprintRequest;
import com.projecttaskmanager.backend.dto.request.sprint.CreateSprintRequest;
import com.projecttaskmanager.backend.dto.response.sprint.SprintResponse;
import com.projecttaskmanager.backend.dto.response.sprint.SprintTaskResponse;

import java.util.List;
import java.util.UUID;

public interface SprintService {
    SprintResponse create(CreateSprintRequest request);
    SprintResponse startSprint(UUID sprintId);
    SprintResponse closeSprint(UUID sprintId);
    SprintTaskResponse addTask(AddTaskToSprintRequest request);
    void removeTask(UUID sprintTaskId);
    List<SprintResponse> getByProject(UUID projectId);
}