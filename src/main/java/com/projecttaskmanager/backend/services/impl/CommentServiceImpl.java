package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.task.CreateCommentRequest;
import com.projecttaskmanager.backend.dto.response.task.CommentResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.mapper.CommentMapper;
import com.projecttaskmanager.backend.models.Comment;
import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.TaskAssignee;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.CommentRepository;
import com.projecttaskmanager.backend.repositories.TaskAssigneeRepository;
import com.projecttaskmanager.backend.repositories.TaskRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.CommentService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.projecttaskmanager.backend.events.NotificationEvent;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectAuthorizationService authorizationService;
    private final CommentMapper commentMapper;
    private final ActivityHelper activityHelper;
    private final ApplicationEventPublisher eventPublisher; // Thêm field này
    private final TaskAssigneeRepository taskAssigneeRepository;

    @Override
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        authorizationService.checkPermission(task.getProject().getId(), "COMMENT_CREATE");

        User user = userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .task(task)
                .user(user)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);

//        List<User> assignees = taskAssigneeRepository.findByTask(task)
//                .stream()
//                .map(TaskAssignee::getUser)
//                .filter(assignee -> !assignee.getId().equals(user.getId())) // Không thông báo cho người comment
//                .toList();
//
//        for (User assignee : assignees) {
//            eventPublisher.publishEvent(
//                    NotificationEvent.builder()
//                            .receiverId(assignee.getId())
//                            .projectId(task.getProject().getId())
//                            .title("Bình luận mới")
//                            .content(String.format("%s đã bình luận về task: %s",
//                                    user.getFullName(), task.getTitle()))
//                            .type("COMMENT_ADDED")
//                            .referenceId(task.getId())
//                            .actorId(user.getId())
//                            .build()
//            );
//        }

        activityHelper.log(
                ActivityAction.COMMENT_CREATED,
                "COMMENT",
                saved.getId(),
                task.getProject().getId(),
                "Comment added",
                user.getId()
        );

        return commentMapper.toResponse(saved);
    }

    @Override
    public CommentResponse updateComment(UUID commentId, CreateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        authorizationService.checkPermission(comment.getTask().getProject().getId(), "COMMENT_UPDATE");

        comment.setContent(request.getContent());

        Comment saved = commentRepository.save(comment);

        activityHelper.log(
                ActivityAction.COMMENT_UPDATED,
                "COMMENT",
                saved.getId(),
                comment.getTask().getProject().getId(),
                "Comment updated",
                comment.getUser().getId()
        );
        return commentMapper.toResponse(saved);
    }

    @Override
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        authorizationService.checkPermission(comment.getTask().getProject().getId(), "COMMENT_DELETE");

        activityHelper.log(
                ActivityAction.COMMENT_DELETED,
                "COMMENT",
                comment.getId(),
                comment.getTask().getProject().getId(),
                "Comment deleted",
                comment.getUser().getId()
        );

        commentRepository.delete(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        authorizationService.checkPermission(task.getProject().getId(), "COMMENT_VIEW");

        return commentRepository.findAllByTask(task)
                .stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse getCommentById(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        authorizationService.checkPermission(comment.getTask().getProject().getId(), "COMMENT_VIEW");

        return commentMapper.toResponse(comment);
    }
}