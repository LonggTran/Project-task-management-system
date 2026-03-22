package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.response.notification.NotificationResponse;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void send(UUID receiverId, String title, String content, String type, UUID referenceId, UUID actorId);
    List<NotificationResponse> getMyNotifications();
    void markAsRead(UUID id);
    long countUnread();
}