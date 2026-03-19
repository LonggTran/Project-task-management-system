package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskStatusRequest;
import com.projecttaskmanager.backend.dto.response.task.TaskStatusResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.TaskStatusMapper;
import com.projecttaskmanager.backend.models.TaskStatus;
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

    @Override
    public TaskStatusResponse create(CreateTaskStatusRequest request) {

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            taskStatusRepository.findByIsDefaultTrue().ifPresent(old -> {
                old.setIsDefault(false);
                taskStatusRepository.save(old);
            });
        }

        TaskStatus status = TaskStatus.builder()
                .name(request.getName())
                .category(request.getCategory())
                .isDefault(request.getIsDefault())
                .sort(request.getSort())
                .build();

        return taskStatusMapper.toResponse(taskStatusRepository.save(status));
    }

    @Override
    public TaskStatusResponse update(UUID id, UpdateTaskStatusRequest request) {

        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            taskStatusRepository.findByIsDefaultTrue().ifPresent(old -> {
                old.setIsDefault(false);
                taskStatusRepository.save(old);
            });
        }

        status.setName(request.getName());
        status.setCategory(request.getCategory());
        status.setIsDefault(request.getIsDefault());
        status.setSort(request.getOrder());

        return taskStatusMapper.toResponse(taskStatusRepository.save(status));
    }

    @Override
    public void delete(UUID id) {
        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        taskStatusRepository.delete(status);
    }

    @Override
    public TaskStatusResponse getById(UUID id) {
        TaskStatus status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        return taskStatusMapper.toResponse(status);
    }

    @Override
    public List<TaskStatusResponse> getAll() {
        return taskStatusRepository.findAllByOrderBySortAsc()
                .stream()
                .map(taskStatusMapper::toResponse)
                .toList();
    }
}