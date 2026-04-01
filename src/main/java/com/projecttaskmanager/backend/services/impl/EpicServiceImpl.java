package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.epic.CreateEpicRequest;
import com.projecttaskmanager.backend.dto.request.epic.UpdateEpicRequest;
import com.projecttaskmanager.backend.dto.response.epic.EpicResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.EpicMapper;
import com.projecttaskmanager.backend.models.Epic;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.EpicRepository;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.EpicService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EpicServiceImpl implements EpicService {

    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EpicMapper epicMapper;
    private final ProjectAuthorizationService authorizationService;
    private final ActivityHelper activityHelper;
    private final RedisService redisService;

    private static final String CACHE_EPIC_LIST = "epics:project:";
    private static final String CACHE_EPIC_DETAIL = "epic:id:";

    @Override
    @Transactional
    public EpicResponse create(CreateEpicRequest request, String email) {
        authorizationService.checkPermission(request.getProjectId(), "EPIC_CREATE");

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

        redisService.delete(CACHE_EPIC_LIST + project.getId());

        return epicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EpicResponse update(UUID epicId, UpdateEpicRequest request, String email) {
        Epic epic = getEpicEntity(epicId);
        UUID projectId = epic.getProject().getId();

        authorizationService.checkPermission(projectId, "EPIC_UPDATE");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getName() != null) epic.setName(request.getName());
        if (request.getDescription() != null) epic.setDescription(request.getDescription());
        if (request.getStartDate() != null) epic.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) epic.setEndDate(request.getEndDate());

        Epic saved = epicRepository.save(epic);

        activityHelper.log(ActivityAction.EPIC_UPDATED, "EPIC", saved.getId(),
                projectId, "Updated epic: " + saved.getName(), user.getId());

        redisService.delete(CACHE_EPIC_DETAIL + epicId);
        redisService.delete(CACHE_EPIC_LIST + projectId);

        return epicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID epicId, String email) {
        Epic epic = getEpicEntity(epicId);
        UUID projectId = epic.getProject().getId();

        authorizationService.checkPermission(projectId, "EPIC_DELETE");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        epicRepository.delete(epic);

        activityHelper.log(ActivityAction.EPIC_DELETED, "EPIC", epicId,
                projectId, "Deleted epic: " + epic.getName(), user.getId());

        redisService.delete(CACHE_EPIC_DETAIL + epicId);
        redisService.delete(CACHE_EPIC_LIST + projectId);
    }

    @Override
    public EpicResponse getById(UUID epicId) {
        String cacheKey = CACHE_EPIC_DETAIL + epicId;

        EpicResponse cached = redisService.get(cacheKey, EpicResponse.class);
        if (cached != null) return cached;

        Epic epic = getEpicEntity(epicId);
        authorizationService.checkPermission(epic.getProject().getId(), "EPIC_VIEW");

        EpicResponse response = epicMapper.toResponse(epic);

        redisService.set(cacheKey, response, 10, TimeUnit.MINUTES);

        return response;
    }

    @Override
    public List<EpicResponse> getAllByProject(UUID projectId) {
        String cacheKey = CACHE_EPIC_LIST + projectId;

        EpicResponse[] cachedData = redisService.get(cacheKey, EpicResponse[].class);
        if (cachedData != null) {
            return Arrays.asList(cachedData);
        }

        authorizationService.checkPermission(projectId, "EPIC_VIEW");

        List<EpicResponse> epics = epicRepository.findAllByProjectId(projectId)
                .stream()
                .map(epicMapper::toResponse)
                .toList();

        redisService.set(cacheKey, epics, 30, TimeUnit.MINUTES);

        return epics;
    }

    private Epic getEpicEntity(UUID id) {
        return epicRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
    }
}