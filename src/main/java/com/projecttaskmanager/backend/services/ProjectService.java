package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.request.project.UpdateProjectRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request);
    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(UUID id);
    ProjectResponse updateProject(UUID id, UpdateProjectRequest request);
    void deleteProject(UUID id);
}