package com.projecttaskmanager.backend.controllers;

import com.projecttaskmanager.backend.dto.response.notification.NotificationResponse;
import com.projecttaskmanager.backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getMy() {
        return notificationService.getMyNotifications();
    }

    @PutMapping("/{id}/read")
    public void read(@PathVariable UUID id) {
        notificationService.markAsRead(id);
    }

    @GetMapping("/unread-count")
    public long count() {
        return notificationService.countUnread();
    }
}