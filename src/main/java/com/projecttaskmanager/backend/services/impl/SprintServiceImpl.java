package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.sprint.AddTaskToSprintRequest;
import com.projecttaskmanager.backend.dto.request.sprint.CreateSprintRequest;
import com.projecttaskmanager.backend.dto.response.sprint.SprintResponse;
import com.projecttaskmanager.backend.dto.response.sprint.SprintTaskResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.events.NotificationEvent;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.SprintMapper;
import com.projecttaskmanager.backend.mapper.SprintTaskMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.models.enums.SprintStatus;
import com.projecttaskmanager.backend.repositories.*;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.RedisService;
import com.projecttaskmanager.backend.services.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private final RedisService redisService;

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

        redisService.delete(CacheKey.sprintsByProject(project.getId()));

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
                        .title("Sprint đã khởi động")
                        .content(String.format("Sprint '%s' đã khởi động", sprint.getName()))
                        .type("SPRINT_STARTED")
                        .referenceId(sprint.getId())
                        .build()
        );

        Sprint saved = sprintRepository.save(sprint);

        invalidateSprintCache(saved);

        return sprintMapper.toResponse(saved);
    }

    @Override
    public SprintResponse closeSprint(UUID sprintId) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        auth.checkPermission(sprint.getProject().getId(), "SPRINT_UPDATE");

        sprint.setStatus(SprintStatus.CLOSED);

        eventPublisher.publishEvent(
                NotificationEvent.builder()
                        .projectId(sprint.getProject().getId())
                        .title("Sprint đã kết thúc")
                        .content(String.format("Sprint '%s' đã kết thúc", sprint.getName()))
                        .type("SPRINT_COMPLETED")
                        .referenceId(sprint.getId())
                        .build()
        );

        Sprint saved = sprintRepository.save(sprint);

        invalidateSprintCache(saved);

        return sprintMapper.toResponse(saved);
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

        SprintTask saved = sprintTaskRepository.save(st);

        redisService.delete(CacheKey.sprintTasks(sprint.getId()));

        return sprintTaskMapper.toResponse(saved);
    }

    @Override
    public void removeTask(UUID sprintTaskId) {

        SprintTask st = sprintTaskRepository.findById(sprintTaskId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (st.getSprint().getStatus() != SprintStatus.PLANNING) {
            throw new RuntimeException("Cannot remove task when sprint is active/closed");
        }

        auth.checkPermission(st.getSprint().getProject().getId(), "SPRINT_UPDATE");

        UUID sprintId = st.getSprint().getId();

        sprintTaskRepository.delete(st);

        redisService.delete(CacheKey.sprintTasks(sprintId));
    }

    @Override
    public List<SprintResponse> getByProject(UUID projectId) {

        String cacheKey = CacheKey.sprintsByProject(projectId);

        SprintResponse[] cached = redisService.get(cacheKey, SprintResponse[].class);
        if (cached != null) return Arrays.asList(cached);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        auth.checkPermission(projectId, "SPRINT_VIEW");

        List<SprintResponse> result = sprintRepository.findByProject(project)
                .stream()
                .map(sprintMapper::toResponse)
                .toList();

        redisService.set(cacheKey, result, 10, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<SprintTaskResponse> getSprintTasks(UUID sprintId) {

        String cacheKey = CacheKey.sprintTasks(sprintId);

        SprintTaskResponse[] cached = redisService.get(cacheKey, SprintTaskResponse[].class);
        if (cached != null) return Arrays.asList(cached);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        auth.checkPermission(sprint.getProject().getId(), "SPRINT_VIEW");

        List<SprintTaskResponse> result = sprintTaskRepository.findBySprint(sprint)
                .stream()
                .map(sprintTaskMapper::toResponse)
                .toList();

        redisService.set(cacheKey, result, 10, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public SprintResponse update(UUID sprintId, CreateSprintRequest request) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new AppException(ErrorCode.SPRINT_NOT_FOUND));

        auth.checkPermission(sprint.getProject().getId(), "SPRINT_UPDATE");

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new AppException(ErrorCode.SPRINT_CANNOT_UPDATE);
        }

        sprint.setName(request.getName());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());

        Sprint saved = sprintRepository.save(sprint);

        invalidateSprintCache(saved);

        return sprintMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID sprintId) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new AppException(ErrorCode.SPRINT_NOT_FOUND));

        auth.checkPermission(sprint.getProject().getId(), "SPRINT_DELETE");

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new AppException(ErrorCode.SPRINT_CANNOT_DELETE);
        }

        sprintTaskRepository.deleteBySprint(sprint);
        sprintRepository.delete(sprint);

        invalidateSprintCache(sprint);
    }

    private void invalidateSprintCache(Sprint sprint) {
        redisService.delete(CacheKey.sprintsByProject(sprint.getProject().getId()));
        redisService.delete(CacheKey.sprintTasks(sprint.getId()));
        redisService.delete(CacheKey.sprintDetail(sprint.getId()));
    }
}