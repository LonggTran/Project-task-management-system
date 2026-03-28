package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.task.AssignTaskRequest;
import com.projecttaskmanager.backend.dto.response.task.TaskAssigneeResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.TaskAssigneeMapper;
import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.TaskAssignee;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.TaskAssigneeRepository;
import com.projecttaskmanager.backend.repositories.TaskRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.TaskAssigneeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.projecttaskmanager.backend.events.NotificationEvent;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAssigneeServiceImpl implements TaskAssigneeService {

    private final TaskAssigneeRepository assigneeRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssigneeMapper mapper;
    private final ActivityHelper activityHelper;
    private final ApplicationEventPublisher eventPublisher;

    private User getCurrentUser() {
        return userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public TaskAssigneeResponse assignTask(AssignTaskRequest request) {

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User currentUser = getCurrentUser();

        assigneeRepository.findByTaskAndUser(task, user).ifPresent(a -> {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        });

        TaskAssignee assignee = TaskAssignee.builder()
                .task(task)
                .user(user)
                .build();

        TaskAssignee saved = assigneeRepository.save(assignee);

        eventPublisher.publishEvent(
                NotificationEvent.builder()
                        .receiverId(user.getId())
                        .projectId(task.getProject().getId())
                        .title("Được gán task mới")
                        .content(String.format("Bạn đã được %s gán vào task: %s",
                                currentUser.getFullName(), task.getTitle()))
                        .type("TASK_ASSIGNED")
                        .referenceId(task.getId())
                        .actorId(currentUser.getId())
                        .build()
        );

        activityHelper.log(
                ActivityAction.TASK_ASSIGNED,
                "TASK",
                task.getId(),
                task.getProject().getId(),
                "Assigned user: " + user.getEmail(),
                user.getId()
        );

        return mapper.toResponse(saved);
    }

    @Override
    public TaskAssigneeResponse assignToMe(UUID taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        assigneeRepository.findByTaskAndUser(task, user)
                .ifPresent(a -> { throw new AppException(ErrorCode.VALIDATION_ERROR); });

        TaskAssignee assignee = TaskAssignee.builder()
                .task(task)
                .user(user)
                .build();

        return mapper.toResponse(assigneeRepository.save(assignee));
    }

    @Override
    public void unassignTask(AssignTaskRequest request) {

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TaskAssignee assignee = assigneeRepository.findByTaskAndUser(task, user)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        assigneeRepository.delete(assignee);

        activityHelper.log(
                ActivityAction.TASK_UPDATED,
                "TASK",
                task.getId(),
                task.getProject().getId(),
                "Unassigned user: " + user.getEmail(),
                user.getId()
        );
    }

    @Override
    public List<TaskAssigneeResponse> getAssignees(UUID taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        return assigneeRepository.findByTask(task)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}