package com.projecttaskmanager.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.response.task.SubtaskResponse;
import com.projecttaskmanager.backend.dto.response.task.SubtaskTreeResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.SubtaskMapper;
import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.repositories.TaskRepository;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.RedisService;
import com.projecttaskmanager.backend.services.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubtaskServiceImpl implements SubtaskService {

    private final TaskRepository taskRepository;
    private final SubtaskMapper subtaskMapper;
    private final RedisService redisService;
    private final EntityHelper entityHelper;

    @Override
    public SubtaskResponse create(UUID parentTaskId, CreateTaskRequest request) {

        Task parent = entityHelper.getTaskOrThrow(parentTaskId);
        TaskStatus status = entityHelper.getTaskStatusOrThrow(request.getStatusId());
        User user = entityHelper.getUserOrThrow(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Task subtask = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(parent.getProject())
                .status(status)
                .priority(request.getPriority())
                .type(request.getType())
                .dueDate(request.getDueDate())
                .estimatedTime(request.getEstimatedTime())
                .createdBy(user)
                .parentTask(parent)
                .build();

        Task saved = taskRepository.save(subtask);

        invalidateSubtaskCacheDeep(parent);

        return subtaskMapper.toResponse(saved);
    }

    @Override
    public List<SubtaskResponse> getByParent(UUID parentTaskId) {

        String cacheKey = CacheKey.subtasksByParent(parentTaskId);

        return redisService.getOrLoad(
                cacheKey,
                new TypeReference<List<SubtaskResponse>>() {},
                () -> taskRepository.findAllByParentTaskId(parentTaskId)
                        .stream()
                        .map(subtaskMapper::toResponse)
                        .toList(),
                10, TimeUnit.MINUTES
        );
    }

    @Override
    public void delete(UUID subtaskId) {

        Task subtask = entityHelper.getTaskOrThrow(subtaskId);

        if (subtask.getParentTask() == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        Task parent = subtask.getParentTask();

        taskRepository.delete(subtask);

        invalidateSubtaskCacheDeep(parent);
    }

    @Override
    public SubtaskTreeResponse getTree(UUID taskId) {

        String cacheKey = CacheKey.subtaskTree(taskId);

        return redisService.getOrLoad(
                cacheKey,
                SubtaskTreeResponse.class,
                () -> {
                    Task root = entityHelper.getTaskOrThrow(taskId);
                    List<Task> allTasks = taskRepository
                            .findAllByProjectId(root.getProject().getId());

                    Map<UUID, List<Task>> childrenMap = allTasks.stream()
                            .filter(t -> t.getParentTask() != null)
                            .collect(Collectors.groupingBy(t -> t.getParentTask().getId()));

                    return buildTreeOptimized(root, childrenMap);
                },
                5, TimeUnit.MINUTES
        );
    }

    private SubtaskTreeResponse buildTreeOptimized(
            Task task,
            Map<UUID, List<Task>> childrenMap
    ) {

        List<SubtaskTreeResponse> children = childrenMap
                .getOrDefault(task.getId(), List.of())
                .stream()
                .map(child -> buildTreeOptimized(child, childrenMap))
                .toList();

        return SubtaskTreeResponse.builder()
                .task(subtaskMapper.toResponse(task))
                .children(children)
                .build();
    }

    private void invalidateSubtaskCacheDeep(Task task) {

        while (task != null) {
            redisService.delete(CacheKey.subtasksByParent(task.getId()));
            redisService.delete(CacheKey.subtaskTree(task.getId()));
            task = task.getParentTask();
        }
    }
}