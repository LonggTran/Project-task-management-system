package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.request.task.CreateCommentRequest;
import com.projecttaskmanager.backend.dto.response.ApiResponse;
import com.projecttaskmanager.backend.dto.response.task.CommentResponse;
import com.projecttaskmanager.backend.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse response = commentService.createComment(taskId, request);
        return ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
                .success(true)
                .message("Comment created successfully")
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByTask(
            @PathVariable UUID taskId
    ) {
        List<CommentResponse> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(ApiResponse.<List<CommentResponse>>builder()
                .success(true)
                .message("Comments fetched successfully")
                .data(comments)
                .timestamp(Instant.now())
                .build());
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(
            @PathVariable UUID commentId
    ) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
                .success(true)
                .message("Comment fetched successfully")
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
                .success(true)
                .message("Comment updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID commentId
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Comment deleted successfully")
                .timestamp(Instant.now())
                .build());
    }
}