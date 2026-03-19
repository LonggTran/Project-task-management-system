package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.ProjectMember;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.repositories.ProjectMemberRepository;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectAuthorizationServiceImpl implements ProjectAuthorizationService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void checkProjectMember(UUID projectId) {
        User user = getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        boolean isMember = projectMemberRepository
                .findByProjectAndUser(project, user)
                .isPresent();

        if (!isMember) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    @Override
    public void checkPermission(UUID projectId, String permission) {
        User user = getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectMember member = projectMemberRepository
                .findByProjectAndUser(project, user)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        boolean hasPermission = member.getProjectRole()
                .getPermissions()
                .stream()
                .anyMatch(p -> p.getName().equals(permission));

        if (!hasPermission) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    @Override
    public void checkAnyPermission(UUID projectId, String... permissions) {
        User user = getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectMember member = projectMemberRepository
                .findByProjectAndUser(project, user)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        boolean hasPermission = member.getProjectRole()
                .getPermissions()
                .stream()
                .anyMatch(p -> Arrays.asList(permissions).contains(p.getName()));

        if (!hasPermission) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
}