package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request);
}