package com.projecttaskmanager.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.epic.CreateEpicRequest;
import com.projecttaskmanager.backend.dto.request.epic.UpdateEpicRequest;
import com.projecttaskmanager.backend.dto.response.epic.EpicResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.EpicMapper;
import com.projecttaskmanager.backend.models.Epic;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.EpicRepository;
import com.projecttaskmanager.backend.services.EpicService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EpicServiceImpl implements EpicService {

    private final EpicRepository epicRepository;
    private final EpicMapper epicMapper;
    private final ProjectAuthorizationService authorizationService;
    private final ActivityHelper activityHelper;
    private final RedisService redisService;
    private final EntityHelper entityHelper;

    private static final long LIST_CACHE_TTL = 30;
    private static final long DETAIL_CACHE_TTL = 10;

    @Override
    @Transactional
    public EpicResponse create(CreateEpicRequest request, String email) {
        authorizationService.checkPermission(request.getProjectId(), "EPIC_CREATE");

        Project project = entityHelper.getProjectOrThrow(request.getProjectId());
        User user = entityHelper.getUserOrThrow(email);

        Epic epic = Epic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .project(project)
                .createdBy(user)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Epic saved = epicRepository.save(epic);

        activityHelper.log(ActivityAction.EPIC_CREATED, "EPIC", saved.getId(),
                project.getId(), "Created epic: " + saved.getName(), user.getId());

        redisService.delete(CacheKey.epicsByProject(project.getId()));

        return epicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EpicResponse update(UUID epicId, UpdateEpicRequest request, String email) {
        Epic epic = entityHelper.getEpicOrThrow(epicId);
        UUID projectId = epic.getProject().getId();

        authorizationService.checkPermission(projectId, "EPIC_UPDATE");

        User user = entityHelper.getUserOrThrow(email);

        if (request.getName() != null) epic.setName(request.getName());
        if (request.getDescription() != null) epic.setDescription(request.getDescription());
        if (request.getStartDate() != null) epic.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) epic.setEndDate(request.getEndDate());

        Epic saved = epicRepository.save(epic);

        activityHelper.log(ActivityAction.EPIC_UPDATED, "EPIC", saved.getId(),
                projectId, "Updated epic: " + saved.getName(), user.getId());

        redisService.delete(CacheKey.epicDetail(epicId));
        redisService.delete(CacheKey.epicsByProject(projectId));

        return epicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID epicId, String email) {
        Epic epic = entityHelper.getEpicOrThrow(epicId);
        UUID projectId = epic.getProject().getId();

        authorizationService.checkPermission(projectId, "EPIC_DELETE");

        User user = entityHelper.getUserOrThrow(email);

        epicRepository.delete(epic);

        activityHelper.log(ActivityAction.EPIC_DELETED, "EPIC", epicId,
                projectId, "Deleted epic: " + epic.getName(), user.getId());

        redisService.delete(CacheKey.epicDetail(epicId));
        redisService.delete(CacheKey.epicsByProject(projectId));
    }

    @Override
    public EpicResponse getById(UUID epicId) {
        Epic epic = entityHelper.getEpicOrThrow(epicId);
        authorizationService.checkPermission(epic.getProject().getId(), "EPIC_VIEW");

        String cacheKey = CacheKey.epicDetail(epicId);

        return redisService.getOrLoad(
                cacheKey,
                EpicResponse.class,
                () -> epicMapper.toResponse(epic),
                DETAIL_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    public List<EpicResponse> getAllByProject(UUID projectId) {
        authorizationService.checkPermission(projectId, "EPIC_VIEW");

        String cacheKey = CacheKey.epicsByProject(projectId);

        return redisService.getOrLoad(
                cacheKey,
                new TypeReference<List<EpicResponse>>() {},
                () -> epicRepository.findAllByProjectId(projectId)
                        .stream()
                        .map(epicMapper::toResponse)
                        .toList(),
                LIST_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }
}