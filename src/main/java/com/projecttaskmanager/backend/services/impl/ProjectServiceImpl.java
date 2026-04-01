package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.request.project.UpdateProjectRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.ProjectMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.baseModels.ProjectMemberId;
import com.projecttaskmanager.backend.repositories.*;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.ProjectService;
import com.projecttaskmanager.backend.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectMapper projectMapper;
    private final RedisService redisService;

    private static final String CACHE_PROJECTS = "projects:user:";
    private static final String CACHE_PROJECT_DETAIL = "project:id:";

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String email) {
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

        // Clear cache danh sách của user
        redisService.delete(CACHE_PROJECTS + email);

        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects(String email) {
        String cacheKey = CACHE_PROJECTS + email;
        ProjectResponse[] cachedData = redisService.get(cacheKey, ProjectResponse[].class);

        if (cachedData != null) {
            return Arrays.asList(cachedData);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<ProjectResponse> projects = projectMemberRepository.findByUser(user)
                .stream()
                .map(ProjectMember::getProject)
                .map(projectMapper::toResponse)
                .toList();

        redisService.set(cacheKey, projects, 10, TimeUnit.MINUTES);
        return projects;
    }

    @Override
    public ProjectResponse getProjectById(UUID id, String email) {
        String cacheKey = CACHE_PROJECT_DETAIL + id;
        ProjectResponse cached = redisService.get(cacheKey, ProjectResponse.class);

        if (cached != null) return cached;

        authorizationService.checkProjectMember(id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectResponse response = projectMapper.toResponse(project);
        redisService.set(cacheKey, response, 5, TimeUnit.MINUTES);

        return response;
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request, String email) {
        authorizationService.checkPermission(id, "PROJECT_UPDATE");

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setIsArchived(request.getIsArchived());

        projectRepository.save(project);
        ProjectResponse response = projectMapper.toResponse(project);

        // Invalidate caches
        redisService.delete(CACHE_PROJECTS + email);
        redisService.delete(CACHE_PROJECT_DETAIL + id);

        return response;
    }

    @Override
    @Transactional
    public void deleteProject(UUID id, String email) {
        authorizationService.checkPermission(id, "PROJECT_DELETE");

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        projectMemberRepository.deleteByProject(project);
        projectRepository.delete(project);

        // Invalidate caches
        redisService.delete(CACHE_PROJECTS + email);
        redisService.delete(CACHE_PROJECT_DETAIL + id);
    }
}