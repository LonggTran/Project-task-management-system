package com.projecttaskmanager.backend.dto.request.task;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String content;
}