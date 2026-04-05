package com.projecttaskmanager.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskRequest;
import com.projecttaskmanager.backend.dto.response.label.LabelResponse;
import com.projecttaskmanager.backend.dto.response.task.TaskResponse;
import com.projecttaskmanager.backend.events.NotificationEvent;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.LabelMapper;
import com.projecttaskmanager.backend.mapper.TaskMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.TaskRepository;
import com.projecttaskmanager.backend.services.RedisService;
import com.projecttaskmanager.backend.services.TaskService;
import com.projecttaskmanager.backend.services.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final EntityHelper entityHelper;
    private final WorkflowService workflowService;
    private final TaskMapper taskMapper;
    private final LabelMapper labelMapper;
    private final ActivityHelper activityHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisService redisService;
    private final TaskRepository taskRepository;

    private static final long LIST_CACHE_TTL = 10;
    private static final long DETAIL_CACHE_TTL = 5;
    private static final long LABEL_CACHE_TTL = 20;

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request, String email) {

        Project project = entityHelper.getProjectOrThrow(request.getProjectId());
        TaskStatus status = entityHelper.getTaskStatusOrThrow(request.getStatusId());
        User creator = entityHelper.getUserOrThrow(email);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .status(status)
                .priority(request.getPriority())
                .type(request.getType())
                .dueDate(request.getDueDate())
                .estimatedTime(request.getEstimatedTime())
                .createdBy(creator)
                .build();

        Task saved = taskRepository.save(task);

        redisService.delete(CacheKey.tasksByProject(project.getId()));

        activityHelper.log(ActivityAction.TASK_CREATED, "TASK", saved.getId(),
                project.getId(), "Created task: " + saved.getTitle(), creator.getId());

        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(UUID taskId, UpdateTaskRequest request, String email) {

        Task task = entityHelper.getTaskOrThrow(taskId);
        User currentUser = entityHelper.getUserOrThrow(email);
        TaskStatus oldStatus = task.getStatus();

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getType() != null) task.setType(request.getType());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getEstimatedTime() != null) task.setEstimatedTime(request.getEstimatedTime());

        if (request.getStatusId() != null) {
            TaskStatus newStatus = entityHelper.getTaskStatusOrThrow(request.getStatusId());

            if (workflowService.hasWorkflow(task.getProject().getId())) {
                workflowService.validateTransition(task.getProject().getId(), oldStatus, newStatus);
            }

            task.setStatus(newStatus);

            if (!oldStatus.getId().equals(newStatus.getId())) {
                publishStatusChangeEvent(task, currentUser, oldStatus, newStatus);
            }
        }

        if (request.getEpicId() != null) {
            task.setEpic(entityHelper.getEpicOrThrow(request.getEpicId()));
        }

        if (request.getParentTaskId() != null) {
            task.setParentTask(entityHelper.getTaskOrThrow(request.getParentTaskId()));
        }

        Task saved = taskRepository.save(task);

        invalidateTaskCache(taskId, saved.getProject().getId());

        activityHelper.log(ActivityAction.TASK_UPDATED, "TASK", saved.getId(),
                saved.getProject().getId(), "Updated task: " + saved.getTitle(), currentUser.getId());

        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID taskId, String email) {

        Task task = entityHelper.getTaskOrThrow(taskId);
        User user = entityHelper.getUserOrThrow(email);

        UUID projectId = task.getProject().getId();

        invalidateTaskCache(taskId, projectId);

        activityHelper.log(ActivityAction.TASK_DELETED, "TASK", taskId,
                projectId, "Deleted task: " + task.getTitle(), user.getId());
    }

    @Override
    public TaskResponse getById(UUID taskId) {

        return redisService.getOrLoad(
                CacheKey.taskDetail(taskId),
                TaskResponse.class,
                () -> taskMapper.toResponse(entityHelper.getTaskOrThrow(taskId)),
                DETAIL_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    public List<TaskResponse> getAllByProject(UUID projectId) {

        return redisService.getOrLoad(
                CacheKey.tasksByProject(projectId),
                new TypeReference<List<TaskResponse>>() {},
                () -> entityHelper.getProjectOrThrow(projectId)
                        .getTasks()
                        .stream()
                        .map(taskMapper::toResponse)
                        .toList(),
                LIST_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    public List<LabelResponse> getTaskLabels(UUID taskId) {

        return redisService.getOrLoad(
                CacheKey.taskLabels(taskId),
                new TypeReference<List<LabelResponse>>() {},
                () -> entityHelper.getTaskOrThrow(taskId)
                        .getLabels()
                        .stream()
                        .map(labelMapper::toResponse)
                        .toList(),
                LABEL_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    @Transactional
    public void addLabelToTask(UUID taskId, UUID labelId, String email) {

        Task task = entityHelper.getTaskOrThrow(taskId);
        Label label = entityHelper.getLabelOrThrow(labelId);
        User user = entityHelper.getUserOrThrow(email);

        if (!label.getProject().getId().equals(task.getProject().getId())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        task.getLabels().add(label);

        invalidateTaskCache(taskId, task.getProject().getId());

        activityHelper.log(ActivityAction.TASK_UPDATED, "TASK", task.getId(),
                task.getProject().getId(), "Added label: " + label.getName(), user.getId());
    }

    @Override
    @Transactional
    public void removeLabelFromTask(UUID taskId, UUID labelId, String email) {

        Task task = entityHelper.getTaskOrThrow(taskId);
        Label label = entityHelper.getLabelOrThrow(labelId);
        User user = entityHelper.getUserOrThrow(email);

        task.getLabels().remove(label);

        invalidateTaskCache(taskId, task.getProject().getId());

        activityHelper.log(ActivityAction.TASK_UPDATED, "TASK", task.getId(),
                task.getProject().getId(), "Removed label: " + label.getName(), user.getId());
    }

    private void invalidateTaskCache(UUID taskId, UUID projectId) {
        redisService.delete(CacheKey.taskDetail(taskId));
        redisService.delete(CacheKey.tasksByProject(projectId));
        redisService.delete(CacheKey.taskLabels(taskId));
    }

    private void publishStatusChangeEvent(Task task, User actor, TaskStatus oldS, TaskStatus newS) {
        eventPublisher.publishEvent(NotificationEvent.builder()
                .projectId(task.getProject().getId())
                .title("Thay đổi trạng thái task")
                .content(String.format("Task '%s' đã được %s chuyển từ %s sang %s",
                        task.getTitle(), actor.getFullName(), oldS.getName(), newS.getName()))
                .type("TASK_STATUS_CHANGED")
                .referenceId(task.getId())
                .actorId(actor.getId())
                .build());
    }
}