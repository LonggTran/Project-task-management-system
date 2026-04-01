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
import com.projecttaskmanager.backend.services.RedisService;
import com.projecttaskmanager.backend.services.TaskService;
import com.projecttaskmanager.backend.services.WorkflowService;
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
    private final RedisService redisService;

    // Định nghĩa các Prefix Cache
    private static final String CACHE_TASK_LIST = "tasks:project:";
    private static final String CACHE_TASK_DETAIL = "task:id:";
    private static final String CACHE_TASK_LABELS = "task:labels:";

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request, String email) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        TaskStatus status = taskStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User creator = getUserByEmail(email);

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

        // Invalidate Cache danh sách task của project
        redisService.delete(CACHE_TASK_LIST + project.getId());

        activityHelper.log(ActivityAction.TASK_CREATED, "TASK", saved.getId(),
                project.getId(), "Created task: " + saved.getTitle(), creator.getId());

        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(UUID taskId, UpdateTaskRequest request, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User currentUser = getUserByEmail(email);
        TaskStatus oldStatus = task.getStatus();

        // Cập nhật các trường cơ bản
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getType() != null) task.setType(request.getType());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getEstimatedTime() != null) task.setEstimatedTime(request.getEstimatedTime());

        // Xử lý trạng thái (Workflow)
        if (request.getStatusId() != null) {
            TaskStatus newStatus = taskStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

            if (workflowService.hasWorkflow(task.getProject().getId())) {
                workflowService.validateTransition(task.getProject().getId(), oldStatus, newStatus);
            }

            task.setStatus(newStatus);

            if (!oldStatus.getId().equals(newStatus.getId())) {
                publishStatusChangeEvent(task, currentUser, oldStatus, newStatus);
            }
        }

        // Xử lý Epic & Parent Task
        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
            task.setEpic(epic);
        }

        if (request.getParentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.getParentTaskId())
                    .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
            task.setParentTask(parentTask);
        }

        Task updated = taskRepository.save(task);

        // XÓA CACHE: Phải xóa cả chi tiết và danh sách project
        invalidateTaskCache(taskId, updated.getProject().getId());

        activityHelper.log(ActivityAction.TASK_UPDATED, "TASK", updated.getId(),
                updated.getProject().getId(), "Updated task: " + updated.getTitle(), currentUser.getId());

        return taskMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        User user = getUserByEmail(email);
        UUID projectId = task.getProject().getId();

        taskRepository.delete(task);

        // XÓA CACHE
        invalidateTaskCache(taskId, projectId);
        redisService.delete(CACHE_TASK_LABELS + taskId);

        activityHelper.log(ActivityAction.TASK_DELETED, "TASK", taskId,
                projectId, "Deleted task: " + task.getTitle(), user.getId());
    }

    @Override
    public TaskResponse getById(UUID taskId) {
        String cacheKey = CACHE_TASK_DETAIL + taskId;
        TaskResponse cached = redisService.get(cacheKey, TaskResponse.class);
        if (cached != null) return cached;

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        TaskResponse response = taskMapper.toResponse(task);
        redisService.set(cacheKey, response, 5, TimeUnit.MINUTES);
        return response;
    }

    @Override
    public List<TaskResponse> getAllByProject(UUID projectId) {
        String cacheKey = CACHE_TASK_LIST + projectId;
        TaskResponse[] cached = redisService.get(cacheKey, TaskResponse[].class);
        if (cached != null) return Arrays.asList(cached);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        List<TaskResponse> tasks = taskRepository.findAllByProject(project)
                .stream()
                .map(taskMapper::toResponse)
                .toList();

        redisService.set(cacheKey, tasks, 10, TimeUnit.MINUTES);
        return tasks;
    }

    @Override
    public List<LabelResponse> getTaskLabels(UUID taskId) {
        String cacheKey = CACHE_TASK_LABELS + taskId;
        LabelResponse[] cached = redisService.get(cacheKey, LabelResponse[].class);
        if (cached != null) return Arrays.asList(cached);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        List<LabelResponse> labels = task.getLabels().stream()
                .map(labelMapper::toResponse)
                .toList();

        redisService.set(cacheKey, labels, 20, TimeUnit.MINUTES);
        return labels;
    }

    @Override
    @Transactional
    public void addLabelToTask(UUID taskId, UUID labelId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));
        User currentUser = getUserByEmail(email);

        if (!label.getProject().getId().equals(task.getProject().getId())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        task.getLabels().add(label);
        taskRepository.save(task);

        // Invalidate cache
        invalidateTaskCache(taskId, task.getProject().getId());
        redisService.delete(CACHE_TASK_LABELS + taskId);

        activityHelper.log(ActivityAction.TASK_UPDATED, "TASK", task.getId(),
                task.getProject().getId(), "Added label: " + label.getName(), currentUser.getId());
    }

    @Override
    @Transactional
    public void removeLabelFromTask(UUID taskId, UUID labelId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));
        User currentUser = getUserByEmail(email);

        task.getLabels().remove(label);
        taskRepository.save(task);

        // Invalidate cache
        invalidateTaskCache(taskId, task.getProject().getId());
        redisService.delete(CACHE_TASK_LABELS + taskId);

        activityHelper.log(ActivityAction.TASK_UPDATED, "TASK", task.getId(),
                task.getProject().getId(), "Removed label: " + label.getName(), currentUser.getId());
    }

    // ================= HELPER METHODS =================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void invalidateTaskCache(UUID taskId, UUID projectId) {
        redisService.delete(CACHE_TASK_DETAIL + taskId);
        redisService.delete(CACHE_TASK_LIST + projectId);
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