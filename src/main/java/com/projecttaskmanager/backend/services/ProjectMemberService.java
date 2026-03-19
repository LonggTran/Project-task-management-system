package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.project.AddMemberRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectMemberResponse;
import com.projecttaskmanager.backend.models.Project;

import java.util.UUID;

public interface ProjectMemberService {
    ProjectMemberResponse addMember(UUID projectId, AddMemberRequest request);
    void removeMember(UUID projectId, UUID userId);
}
