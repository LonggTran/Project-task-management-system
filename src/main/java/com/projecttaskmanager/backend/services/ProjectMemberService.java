package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.project.AddMemberRequest;

import java.util.UUID;

public interface ProjectMemberService {
    void addMember(UUID projectId, AddMemberRequest request);
}
