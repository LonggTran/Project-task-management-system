package com.projecttaskmanager.backend.dto.response.task;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private String content;
    private TaskResponse task;
    private UserResponse user; // ai tạo comment
    private Instant createdAt;
    private Instant updatedAt;
}