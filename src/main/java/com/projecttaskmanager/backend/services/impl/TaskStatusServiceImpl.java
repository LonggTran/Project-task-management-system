package com.projecttaskmanager.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.task.CreateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.TaskStatusMapper;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import com.projecttaskmanager.backend.services.RedisService;
import com.projecttaskmanager.backend.services.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;
    private final EntityHelper entityHelper;
    private final RedisService redisService;

    @Override
    public TaskStatusResponse create(UUID projectId, CreateTaskStatusRequest request) {

        Project project = entityHelper.getProjectOrThrow(projectId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            taskStatusRepository.findByIsDefaultTrueAndProject(project).ifPresent(old -> {
                old.setIsDefault(false);
                taskStatusRepository.save(old);
            });
        }

        if (taskStatusRepository.findByNameAndProject(request.getName(), project).isPresent()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        Integer sort = request.getSort();
        if (sort == null) {
            List<TaskStatus> existing = taskStatusRepository.findByProjectOrderBySortAsc(project);
            sort = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getSort() + 1;
        }

        TaskStatus status = TaskStatus.builder()
                .name(request.getName())
                .category(request.getCategory())
                .isDefault(request.getIsDefault())
                .sort(sort)
                .project(project)
                .build();

        TaskStatus saved = taskStatusRepository.save(status);

        invalidateCache(projectId, saved.getId());

        return taskStatusMapper.toResponse(saved);
    }

    @Override
    public TaskStatusResponse update(UUID id, UpdateTaskStatusRequest request) {

        TaskStatus status = entityHelper.getTaskStatusOrThrow(id);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            taskStatusRepository.findByIsDefaultTrueAndProject(status.getProject()).ifPresent(old -> {
                if (!old.getId().equals(status.getId())) {
                    old.setIsDefault(false);
                    taskStatusRepository.save(old);
                }
            });
        }

        if (request.getName() != null) status.setName(request.getName());
        if (request.getCategory() != null) status.setCategory(request.getCategory());
        if (request.getIsDefault() != null) status.setIsDefault(request.getIsDefault());
        if (request.getOrder() != null) status.setSort(request.getOrder());

        TaskStatus saved = taskStatusRepository.save(status);

        invalidateCache(status.getProject().getId(), id);

        return taskStatusMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {

        TaskStatus status = entityHelper.getTaskStatusOrThrow(id);

        if (Boolean.TRUE.equals(status.getIsDefault())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        taskStatusRepository.delete(status);

        invalidateCache(status.getProject().getId(), id);
    }

    @Override
    public List<TaskStatusResponse> getAll() {
        return taskStatusRepository.findAllByOrderBySortAsc()
                .stream()
                .map(taskStatusMapper::toResponse)
                .toList();
    }

    @Override
    public TaskStatusResponse getById(UUID id) {

        String key = CacheKey.taskStatus(id);

        return redisService.getOrLoad(
                key,
                new TypeReference<TaskStatusResponse>() {},
                () -> taskStatusMapper.toResponse(
                        entityHelper.getTaskStatusOrThrow(id)
                ),
                10, TimeUnit.MINUTES
        );
    }

    @Override
    public List<TaskStatusResponse> getByProject(UUID projectId) {

        String key = CacheKey.taskStatuses(projectId);

        return redisService.getOrLoad(
                key,
                new TypeReference<List<TaskStatusResponse>>() {},
                () -> {
                    Project project = entityHelper.getProjectOrThrow(projectId);

                    return taskStatusRepository.findByProjectOrderBySortAsc(project)
                            .stream()
                            .map(taskStatusMapper::toResponse)
                            .toList();
                },
                10, TimeUnit.MINUTES
        );
    }

    @Override
    public TaskStatusResponse getDefaultStatus(UUID projectId) {

        String key = CacheKey.defaultTaskStatus(projectId);

        return redisService.getOrLoad(
                key,
                new TypeReference<TaskStatusResponse>() {},
                () -> {
                    Project project = entityHelper.getProjectOrThrow(projectId);

                    TaskStatus defaultStatus = taskStatusRepository
                            .findByProjectIdAndIsDefaultTrue(projectId)
                            .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

                    return taskStatusMapper.toResponse(defaultStatus);
                },
                10, TimeUnit.MINUTES
        );
    }

    @Override
    @Transactional
    public void reorderStatuses(UUID projectId, List<Map<String, Object>> statuses) {

        entityHelper.getProjectOrThrow(projectId);

        for (Map<String, Object> s : statuses) {
            UUID id = UUID.fromString((String) s.get("id"));
            Integer sort = (Integer) s.get("sort");

            TaskStatus status = entityHelper.getTaskStatusOrThrow(id);

            if (!status.getProject().getId().equals(projectId)) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }

            status.setSort(sort);
            taskStatusRepository.save(status);
        }

        redisService.delete(CacheKey.taskStatuses(projectId));
    }

    private void invalidateCache(UUID projectId, UUID statusId) {
        redisService.delete(CacheKey.taskStatuses(projectId));
        redisService.delete(CacheKey.defaultTaskStatus(projectId));
        redisService.delete(CacheKey.taskStatus(statusId));
    }
}