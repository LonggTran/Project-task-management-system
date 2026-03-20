package com.projecttaskmanager.backend.dto.response.attachment;

import com.projecttaskmanager.backend.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AttachmentResponse {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private UUID taskId;
    private UserResponse uploadedBy;
    private Instant uploadedAt;
}