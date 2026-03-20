package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.task.CommentResponse;
import com.projecttaskmanager.backend.models.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserMapper userMapper;
    private final TaskMapper taskMapper;

    public CommentResponse toResponse(Comment comment) {
        if (comment == null) return null;

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .task(taskMapper.toResponse(comment.getTask()))
                .user(userMapper.toResponse(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}