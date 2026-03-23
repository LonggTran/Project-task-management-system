package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.sprint.AddTaskToSprintRequest;
import com.projecttaskmanager.backend.dto.request.sprint.CreateSprintRequest;
import com.projecttaskmanager.backend.dto.response.sprint.SprintResponse;
import com.projecttaskmanager.backend.dto.response.sprint.SprintTaskResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.SprintMapper;
import com.projecttaskmanager.backend.mapper.SprintTaskMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.models.enums.SprintStatus;
import com.projecttaskmanager.backend.repositories.*;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.projecttaskmanager.backend.events.NotificationEvent;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SprintMapper sprintMapper;
    private final SprintTaskMapper sprintTaskMapper;
    private final ProjectAuthorizationService auth;
    private final ActivityHelper activityHelper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public SprintResponse create(CreateSprintRequest request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        auth.checkPermission(project.getId(), "SPRINT_CREATE");

        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.PLANNING)
                .project(project)
                .build();

        Sprint saved = sprintRepository.save(sprint);

        eventPublisher.publishEvent(
                NotificationEvent.builder()
                        .projectId(sprint.getProject().getId())
                        .title("Sprint đã bắt đầu")
                        .content(String.format("Sprint '%s' đã bắt đầu", sprint.getName()))
                        .type("SPRINT_STARTED")
                        .referenceId(sprint.getId())
                        .build()
        );

        activityHelper.log(
                ActivityAction.SPRINT_CREATED,
                "SPRINT",
                saved.getId(),
                project.getId(),
                "Created sprint: " + saved.getName(),
                null
        );

        return sprintMapper.toResponse(saved);
    }

    @Override
    public SprintResponse startSprint(UUID sprintId) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        auth.checkPermission(sprint.getProject().getId(), "SPRINT_UPDATE");

        sprint.setStatus(SprintStatus.ACTIVE);

        eventPublisher.publishEvent(
                NotificationEvent.builder()
                        .projectId(sprint.getProject().getId())
                        .title("Sprint đã kết thúc")
                        .content(String.format("Sprint '%s' đã kết thúc", sprint.getName()))
                        .type("SPRINT_COMPLETED")
                        .referenceId(sprint.getId())
                        .build()
        );

        activityHelper.log(
                ActivityAction.SPRINT_STARTED,
                "SPRINT",
                sprint.getId(),
                sprint.getProject().getId(),
                "Sprint started",
                null
        );

        return sprintMapper.toResponse(sprintRepository.save(sprint));
    }

    @Override
    public SprintResponse closeSprint(UUID sprintId) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        auth.checkPermission(sprint.getProject().getId(), "SPRINT_UPDATE");

        sprint.setStatus(SprintStatus.CLOSED);

        activityHelper.log(
                ActivityAction.SPRINT_COMPLETED,
                "SPRINT",
                sprint.getId(),
                sprint.getProject().getId(),
                "Sprint completed",
                null
        );

        return sprintMapper.toResponse(sprintRepository.save(sprint));
    }

    @Override
    public SprintTaskResponse addTask(AddTaskToSprintRequest request) {

        Sprint sprint = sprintRepository.findById(request.getSprintId())
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new RuntimeException("Cannot add task when sprint is not PLANNING");
        }

        auth.checkPermission(sprint.getProject().getId(), "SPRINT_UPDATE");

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (sprintTaskRepository.findBySprintAndTask(sprint, task).isPresent()) {
            throw new RuntimeException("Task already in sprint");
        }

        SprintTask st = SprintTask.builder()
                .sprint(sprint)
                .task(task)
                .build();

        return sprintTaskMapper.toResponse(sprintTaskRepository.save(st));
    }

    @Override
    public void removeTask(UUID sprintTaskId) {

        SprintTask st = sprintTaskRepository.findById(sprintTaskId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (st.getSprint().getStatus() != SprintStatus.PLANNING) {
            throw new RuntimeException("Cannot remove task when sprint is active/closed");
        }

        auth.checkPermission(st.getSprint().getProject().getId(), "SPRINT_UPDATE");

        sprintTaskRepository.delete(st);
    }

    @Override
    public List<SprintResponse> getByProject(UUID projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        auth.checkPermission(projectId, "SPRINT_VIEW");

        return sprintRepository.findByProject(project)
                .stream()
                .map(sprintMapper::toResponse)
                .toList();
    }
}