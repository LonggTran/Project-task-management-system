package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.TaskStatusMapper;
import com.projecttaskmanager.backend.models.Project;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.repositories.ProjectRepository;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import com.projecttaskmanager.backend.services.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;
    private final ProjectRepository projectRepository;

    @Override
    public TaskStatusResponse create(UUID projectId, CreateTaskStatusRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

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
            List<TaskStatus> existingStatuses = taskStatusRepository.findByProjectOrderBySortAsc(project);
            sort = existingStatuses.isEmpty() ? 1 : existingStatuses.get(existingStatuses.size() - 1).getSort() + 1;
        }

        TaskStatus status = TaskStatus.builder()
                .name(request.getName())
                .category(request.getCategory())
                .isDefault(request.getIsDefault())
                .sort(sort)
                .project(project)
                .build();

        return taskStatusMapper.toResponse(taskStatusRepository.save(status));
    }

    @Override
    public TaskStatusResponse update(UUID id, UpdateTaskStatusRequest request) {

        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

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

        return taskStatusMapper.toResponse(taskStatusRepository.save(status));
    }

    @Override
    public void delete(UUID id) {
        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (Boolean.TRUE.equals(status.getIsDefault())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        taskStatusRepository.delete(status);
    }

    @Override
    public TaskStatusResponse getById(UUID id) {
        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        return taskStatusMapper.toResponse(status);
    }

    @Override
    public List<TaskStatusResponse> getByProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        return taskStatusRepository.findByProjectOrderBySortAsc(project)
                .stream()
                .map(taskStatusMapper::toResponse)
                .toList();
    }

    @Override
    public List<TaskStatusResponse> getAll() {
        return taskStatusRepository.findAllByOrderBySortAsc()
                .stream()
                .map(taskStatusMapper::toResponse)
                .toList();
    }

    @Override
    public TaskStatusResponse getDefaultStatus(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        TaskStatus defaultStatus = taskStatusRepository.findByIsDefaultTrueAndProject(project)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        return taskStatusMapper.toResponse(defaultStatus);
    }
}