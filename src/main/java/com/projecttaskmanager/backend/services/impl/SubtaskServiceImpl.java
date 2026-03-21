package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.response.task.SubtaskResponse;
import com.projecttaskmanager.backend.dto.response.task.SubtaskTreeResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.SubtaskMapper;
import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.TaskStatus;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.repositories.TaskRepository;
import com.projecttaskmanager.backend.repositories.TaskStatusRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubtaskServiceImpl implements SubtaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final SubtaskMapper subtaskMapper;

    @Override
    public SubtaskResponse create(UUID parentTaskId, CreateTaskRequest request) {

        Task parent = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        TaskStatus status = taskStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User user = userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

        return subtaskMapper.toResponse(taskRepository.save(subtask));
    }

    @Override
    public List<SubtaskResponse> getByParent(UUID parentTaskId) {

        return taskRepository.findAllByParentTaskId(parentTaskId)
                .stream()
                .map(subtaskMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID subtaskId) {

        Task subtask = taskRepository.findById(subtaskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (subtask.getParentTask() == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        taskRepository.delete(subtask);
    }

    @Override
    public SubtaskTreeResponse getTree(UUID taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        return buildTree(task);
    }

    private SubtaskTreeResponse buildTree(Task task) {

        return SubtaskTreeResponse.builder()
                .task(subtaskMapper.toResponse(task))
                .children(
                        task.getSubtasks() == null ? List.of() :
                                task.getSubtasks()
                                        .stream()
                                        .map(this::buildTree)
                                        .toList()
                )
                .build();
    }
}