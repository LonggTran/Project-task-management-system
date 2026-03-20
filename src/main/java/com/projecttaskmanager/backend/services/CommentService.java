package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.request.task.CreateCommentRequest;
import com.projecttaskmanager.backend.dto.response.task.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponse createComment(UUID taskId, CreateCommentRequest request);
    CommentResponse updateComment(UUID commentId, CreateCommentRequest request);
    void deleteComment(UUID commentId);
    CommentResponse getCommentById(UUID commentId);
    List<CommentResponse> getCommentsByTask(UUID taskId);
}