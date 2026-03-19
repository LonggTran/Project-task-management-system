package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.project.AddMemberRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectMemberResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.ProjectMemberMapper;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.ProjectMember;
import com.projecttaskmanager.backend.models.ProjectRole;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.baseModels.ProjectMemberId;
import com.projecttaskmanager.backend.repositories.ProjectMemberRepository;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.repositories.ProjectRoleRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectMemberMapper projectMemberMapper;

    @Override
    public ProjectMemberResponse addMember(UUID projectId, AddMemberRequest request) {
        authorizationService.checkPermission(projectId, "MEMBER_ADD");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectRole role = projectRoleRepository.findByName(request.getProjectRole())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        boolean exists = projectMemberRepository
                .findByProjectAndUser(project, user)
                .isPresent();

        if (exists) {
            throw new AppException(ErrorCode.USER_ALREADY_IN_PROJECT);
        }

        ProjectMember member = ProjectMember.builder()
                .id(new ProjectMemberId(projectId, user.getId()))
                .project(project)
                .user(user)
                .projectRole(role)
                .joinedAt(Instant.now())
                .build();

        ProjectMember saved = projectMemberRepository.save(member);

        return projectMemberMapper.toResponse(saved);
    }

    @Override
    public void removeMember(UUID projectId, UUID userId) {

        authorizationService.checkPermission(projectId, "MEMBER_REMOVE");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectMember member = projectMemberRepository
                .findByProjectAndUser(project, user)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        if (member.getProjectRole().getName().equals("OWNER")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        projectMemberRepository.delete(member);
    }
}