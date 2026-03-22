package com.projecttaskmanager.backend.dto.response.notification;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String content;
    private boolean isRead;
    private String type;
    private UUID referenceId;
    private Instant createdAt;
}