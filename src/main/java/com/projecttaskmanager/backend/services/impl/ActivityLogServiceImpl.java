package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.response.activity.ActivityResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.ActivityMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.ActivityLogRepository;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.ActivityLogService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityMapper activityMapper;
    private final ProjectAuthorizationService authorizationService;

    private User currentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void log(ActivityAction action,
                    String entityType,
                    UUID entityId,
                    UUID projectId,
                    String metadata,
                    UUID actorId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ActivityLog log = ActivityLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .project(project)
                .actor(actor)
                .metadata(metadata)
                .createdAt(Instant.now())
                .build();

        activityRepository.save(log);
    }

    @Override
    public List<ActivityResponse> getProjectActivities(UUID projectId) {


        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        return activityRepository.findByProjectOrderByCreatedAtDesc(project)
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    public List<ActivityResponse> getMyActivities() {
        return activityRepository.findByActorIdOrderByCreatedAtDesc(currentUser().getId())
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }
}