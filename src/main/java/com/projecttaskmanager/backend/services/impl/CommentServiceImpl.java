package com.projecttaskmanager.backend.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.projecttaskmanager.backend.constants.CacheKey;
import com.projecttaskmanager.backend.dto.request.task.CreateCommentRequest;
import com.projecttaskmanager.backend.dto.response.task.CommentResponse;
import com.projecttaskmanager.backend.events.ActivityHelper;
import com.projecttaskmanager.backend.helpers.EntityHelper;
import com.projecttaskmanager.backend.mapper.CommentMapper;
import com.projecttaskmanager.backend.models.Comment;
import com.projecttaskmanager.backend.models.Task;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.models.enums.ActivityAction;
import com.projecttaskmanager.backend.repositories.CommentRepository;
import com.projecttaskmanager.backend.services.CommentService;
import com.projecttaskmanager.backend.services.ProjectAuthorizationService;
import com.projecttaskmanager.backend.services.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EntityHelper entityHelper;
    private final ProjectAuthorizationService authorizationService;
    private final CommentMapper commentMapper;
    private final ActivityHelper activityHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisService redisService;

    private static final long LIST_CACHE_TTL = 10;
    private static final long DETAIL_CACHE_TTL = 5;

    @Override
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request) {

        Task task = entityHelper.getTaskOrThrow(taskId);

        authorizationService.checkPermission(task.getProject().getId(), "COMMENT_CREATE");

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = entityHelper.getUserOrThrow(email);

        Comment comment = Comment.builder()
                .task(task)
                .user(user)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);

        redisService.delete(CacheKey.commentsByTask(taskId));

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

        Comment comment = entityHelper.getCommentOrThrow(commentId);

        authorizationService.checkPermission(comment.getTask().getProject().getId(), "COMMENT_UPDATE");

        comment.setContent(request.getContent());

        comment = commentRepository.save(comment);

        invalidateCommentCache(commentId, comment.getTask().getId());

        activityHelper.log(
                ActivityAction.COMMENT_UPDATED,
                "COMMENT",
                comment.getId(),
                comment.getTask().getProject().getId(),
                "Comment updated",
                comment.getUser().getId()
        );

        return commentMapper.toResponse(comment);
    }

    @Override
    public void deleteComment(UUID commentId) {

        Comment comment = entityHelper.getCommentOrThrow(commentId);

        authorizationService.checkPermission(comment.getTask().getProject().getId(), "COMMENT_DELETE");

        UUID taskId = comment.getTask().getId();

        commentRepository.delete(comment);

        invalidateCommentCache(commentId, taskId);

        activityHelper.log(
                ActivityAction.COMMENT_DELETED,
                "COMMENT",
                commentId,
                comment.getTask().getProject().getId(),
                "Comment deleted",
                comment.getUser().getId()
        );
    }

    @Override
    public List<CommentResponse> getCommentsByTask(UUID taskId) {

        String cacheKey = CacheKey.commentsByTask(taskId);

        return redisService.getOrLoad(
                cacheKey,
                new TypeReference<List<CommentResponse>>() {},
                () -> {
                    Task task = entityHelper.getTaskOrThrow(taskId);

                    authorizationService.checkPermission(
                            task.getProject().getId(),
                            "COMMENT_VIEW"
                    );

                    return commentRepository.findAllByTask(task)
                            .stream()
                            .map(commentMapper::toResponse)
                            .toList();
                },
                LIST_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    public CommentResponse getCommentById(UUID commentId) {

        String cacheKey = CacheKey.commentDetail(commentId);

        return redisService.getOrLoad(
                cacheKey,
                CommentResponse.class,
                () -> {
                    Comment comment = entityHelper.getCommentOrThrow(commentId);

                    authorizationService.checkPermission(
                            comment.getTask().getProject().getId(),
                            "COMMENT_VIEW"
                    );

                    return commentMapper.toResponse(comment);
                },
                DETAIL_CACHE_TTL,
                TimeUnit.MINUTES
        );
    }

    private void invalidateCommentCache(UUID commentId, UUID taskId) {
        redisService.delete(CacheKey.commentDetail(commentId));
        redisService.delete(CacheKey.commentsByTask(taskId));
    }
}