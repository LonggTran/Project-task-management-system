package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.request.project.UpdateProjectRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request, String email);
    List<ProjectResponse> getAllProjects(String email);
    ProjectResponse getProjectById(UUID id, String email);
    ProjectResponse updateProject(UUID id, UpdateProjectRequest request, String email);
    void deleteProject(UUID id, String email);
}