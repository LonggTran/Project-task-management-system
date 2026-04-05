package com.projecttaskmanager.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.project.CreateProjectRequest;
import com.projecttaskmanager.backend.dto.request.project.UpdateProjectRequest;
import com.projecttaskmanager.backend.dto.response.project.ProjectResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.ProjectMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.baseModels.ProjectMemberId;
import com.projecttaskmanager.backend.repositories.ProjectMemberRepository;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.repositories.ProjectRoleRepository;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.ProjectService;
import com.projecttaskmanager.backend.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectMapper projectMapper;
    private final RedisService redisService;
    private final EntityHelper entityHelper;

    private static final long LIST_CACHE_TTL = 10;
    private static final long DETAIL_CACHE_TTL = 5;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String email) {

        User user = entityHelper.getUserOrThrow(email);

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

        redisService.delete(CacheKey.userProjects(email));

        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects(String email) {

        String cacheKey = CacheKey.userProjects(email);

        return redisService.getOrLoad(
                cacheKey,
                new TypeReference<List<ProjectResponse>>() {},
                () -> {
                    User user = entityHelper.getUserOrThrow(email);

                    return projectMemberRepository.findByUser(user)
                            .stream()
                            .map(ProjectMember::getProject)
                            .map(projectMapper::toResponse)
                            .toList();
                },
                LIST_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    public ProjectResponse getProjectById(UUID id, String email) {
        authorizationService.checkProjectMember(id);

        String cacheKey = CacheKey.projectDetail(id);

        return redisService.getOrLoad(
                cacheKey,
                ProjectResponse.class,
                () -> {
                    Project project = entityHelper.getProjectOrThrow(id);
                    return projectMapper.toResponse(project);
                },
                DETAIL_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request, String email) {

        authorizationService.checkPermission(id, "PROJECT_UPDATE");

        Project project = entityHelper.getProjectOrThrow(id);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setIsArchived(request.getIsArchived());

        projectRepository.save(project);

        ProjectResponse response = projectMapper.toResponse(project);

        invalidateProjectCaches(project);

        return response;
    }

    @Override
    @Transactional
    public void deleteProject(UUID id, String email) {

        authorizationService.checkPermission(id, "PROJECT_DELETE");

        Project project = entityHelper.getProjectOrThrow(id);

        List<ProjectMember> members = projectMemberRepository.findByProject(project);

        projectMemberRepository.deleteAll(members);
        projectRepository.delete(project);

        invalidateProjectCaches(project, members);
    }

    private void invalidateProjectCaches(Project project) {
        List<ProjectMember> members = projectMemberRepository.findByProject(project);
        invalidateProjectCaches(project, members);
    }

    private void invalidateProjectCaches(Project project, List<ProjectMember> members) {

        redisService.delete(CacheKey.projectDetail(project.getId()));

        for (ProjectMember member : members) {
            String email = member.getUser().getEmail();
            redisService.delete(CacheKey.userProjects(email));
        }
    }
}