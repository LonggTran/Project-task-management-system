package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.notification.NotificationResponse;
import com.projecttaskmanager.backend.models.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .isRead(n.isRead())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}