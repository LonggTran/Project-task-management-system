package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.task.CreateTaskRequest;
import com.projecttaskmanager.backend.dto.request.task.UpdateTaskRequest;
import com.projecttaskmanager.backend.dto.response.label.LabelResponse;
import com.projecttaskmanager.backend.dto.response.task.TaskResponse;
import com.projecttaskmanager.backend.events.NotificationEvent;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.LabelMapper;
import com.projecttaskmanager.backend.mapper.TaskMapper;
import com.projecttaskmanager.backend.models.*;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.*;
import com.projecttaskmanager.backend.services.TaskService;
import com.projecttaskmanager.backend.services.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final WorkflowService workflowService;
    private final TaskMapper taskMapper;
    private final ActivityHelper activityHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final EpicRepository epicRepository;
    private final LabelMapper labelMapper;
    private final LabelRepository labelRepository;

    private User getCurrentUser() {
        return userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public TaskResponse create(CreateTaskRequest request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        TaskStatus status = taskStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User creator = userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

        activityHelper.log(
                ActivityAction.TASK_CREATED,
                "TASK",
                saved.getId(),
                project.getId(),
                "Created task: " + saved.getTitle(),
                creator.getId()
        );

        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse update(UUID taskId, UpdateTaskRequest request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        TaskStatus oldStatus = task.getStatus();

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());

        if (request.getStatusId() != null) {
            TaskStatus newStatus = taskStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

            // Kiểm tra xem project có workflow không
            boolean hasWorkflow = workflowService.hasWorkflow(task.getProject().getId());

            if (hasWorkflow) {
                // Nếu có workflow, validate transition
                workflowService.validateTransition(
                        task.getProject().getId(),
                        task.getStatus(),
                        newStatus
                );
            }
            // Nếu chưa có workflow, cho phép chuyển trạng thái tự do

            task.setStatus(newStatus);

            if (!oldStatus.getId().equals(newStatus.getId())) {
                User currentUser = getCurrentUser();
                eventPublisher.publishEvent(
                        NotificationEvent.builder()
                                .projectId(task.getProject().getId())
                                .title("Thay đổi trạng thái task")
                                .content(String.format("Task '%s' đã được %s thay đổi trạng thái từ %s sang %s",
                                        task.getTitle(), currentUser.getFullName(), oldStatus.getName(), newStatus.getName()))
                                .type("TASK_STATUS_CHANGED")
                                .referenceId(task.getId())
                                .actorId(currentUser.getId())
                                .build()
                );
            }
        }

        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
            task.setEpic(epic);
        } else if (request.getEpicId() == null && request.getEpicId() != null) {
            task.setEpic(null);
        }

        if (request.getParentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.getParentTaskId())
                    .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
            task.setParentTask(parentTask);
        }
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getType() != null) task.setType(request.getType());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getEstimatedTime() != null) task.setEstimatedTime(request.getEstimatedTime());

        Task updated = taskRepository.save(task);

        User user = userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        activityHelper.log(
                ActivityAction.TASK_UPDATED,
                "TASK",
                updated.getId(),
                updated.getProject().getId(),
                "Updated task: " + updated.getTitle(),
                user.getId()
        );

        return taskMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        User user = userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        activityHelper.log(
                ActivityAction.TASK_DELETED,
                "TASK",
                task.getId(),
                task.getProject().getId(),
                "Deleted task: " + task.getTitle(),
                user.getId()
        );

        taskRepository.delete(task);
    }

    @Override
    public TaskResponse getById(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        return taskMapper.toResponse(task);
    }

    @Override
    public List<TaskResponse> getAllByProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        return taskRepository.findAllByProject(project)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    public List<LabelResponse> getTaskLabels(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        return task.getLabels().stream()
                .map(labelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addLabelToTask(UUID taskId, UUID labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        // Kiểm tra label thuộc cùng project
        if (!label.getProject().getId().equals(task.getProject().getId())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        task.getLabels().add(label);
        taskRepository.save(task);

        // Log activity
        User currentUser = getCurrentUser();
        activityHelper.log(
                ActivityAction.TASK_UPDATED,
                "TASK",
                task.getId(),
                task.getProject().getId(),
                "Added label: " + label.getName(),
                currentUser.getId()
        );
    }

    @Override
    @Transactional
    public void removeLabelFromTask(UUID taskId, UUID labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        task.getLabels().remove(label);
        taskRepository.save(task);

        // Log activity
        User currentUser = getCurrentUser();
        activityHelper.log(
                ActivityAction.TASK_UPDATED,
                "TASK",
                task.getId(),
                task.getProject().getId(),
                "Removed label: " + label.getName(),
                currentUser.getId()
        );
    }
}