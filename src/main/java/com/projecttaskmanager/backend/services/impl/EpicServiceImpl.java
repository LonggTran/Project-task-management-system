package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.epic.CreateEpicRequest;
import com.projecttaskmanager.backend.dto.request.epic.UpdateEpicRequest;
import com.projecttaskmanager.backend.dto.response.epic.EpicResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.events.NotificationEvent;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EpicServiceImpl implements EpicService {

    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EpicMapper epicMapper;
    private final ProjectAuthorizationService authorizationService;
    private final ApplicationEventPublisher eventPublisher;
    private final ActivityHelper activityHelper;

    @Override
    public EpicResponse create(CreateEpicRequest request) {

        authorizationService.checkPermission(request.getProjectId(), "EPIC_CREATE");

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User user = getCurrentUser();

        Epic epic = Epic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .project(project)
                .createdBy(user)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Epic saved = epicRepository.save(epic);

        activityHelper.log(
                ActivityAction.EPIC_CREATED,
                "EPIC",
                saved.getId(),
                project.getId(),
                "Created epic: " + saved.getName(),
                user.getId()
        );

        return epicMapper.toResponse(saved);
    }

    @Override
    public EpicResponse update(UUID epicId, UpdateEpicRequest request) {

        Epic epic = getEpic(epicId);

        authorizationService.checkPermission(epic.getProject().getId(), "EPIC_UPDATE");

        if (request.getName() != null) epic.setName(request.getName());
        if (request.getDescription() != null) epic.setDescription(request.getDescription());
        if (request.getStartDate() != null) epic.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) epic.setEndDate(request.getEndDate());

        return epicMapper.toResponse(epicRepository.save(epic));
    }

    @Override
    public void delete(UUID epicId) {

        Epic epic = getEpic(epicId);

        authorizationService.checkPermission(epic.getProject().getId(), "EPIC_DELETE");

        epicRepository.delete(epic);
    }

    @Override
    public EpicResponse getById(UUID epicId) {

        Epic epic = getEpic(epicId);

        authorizationService.checkPermission(epic.getProject().getId(), "EPIC_VIEW");

        return epicMapper.toResponse(epic);
    }

    @Override
    public List<EpicResponse> getAllByProject(UUID projectId) {

        authorizationService.checkPermission(projectId, "EPIC_VIEW");

        return epicRepository.findAllByProjectId(projectId)
                .stream()
                .map(epicMapper::toResponse)
                .toList();
    }

    // ================= HELPER =================

    private Epic getEpic(UUID id) {
        return epicRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}