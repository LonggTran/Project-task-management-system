package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.config.ProjectStatusSeeder;
import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.request.project.UpdateProjectRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.ProjectMapper;
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
import com.projecttaskmanager.backend.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectMapper projectMapper;
    private final ProjectStatusSeeder projectStatusSeeder;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectRole ownerRole = projectRoleRepository.findByName("OWNER")
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isArchived(false)
                .owner(user)
                .build();

        projectRepository.save(project);

        ProjectMember owner = ProjectMember.builder()
                .id(new ProjectMemberId(project.getId(), user.getId()))
                .project(project)
                .user(user)
                .projectRole(ownerRole)
                .joinedAt(Instant.now())
                .build();

        projectMemberRepository.save(owner);
        projectStatusSeeder.seedDefaultStatusesForProject(project);
        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return projectMemberRepository.findByUser(user)
                .stream()
                .map(ProjectMember::getProject)
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    public ProjectResponse getProjectById(UUID id) {

        authorizationService.checkProjectMember(id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {

        authorizationService.checkPermission(id, "PROJECT_UPDATE");

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setIsArchived(request.getIsArchived());

        projectRepository.save(project);

        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {

        authorizationService.checkPermission(id, "PROJECT_DELETE");

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        projectMemberRepository.deleteByProject(project);
        projectRepository.delete(project);
    }
}
