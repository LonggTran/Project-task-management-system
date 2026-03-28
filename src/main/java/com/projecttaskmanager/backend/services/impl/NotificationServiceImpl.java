package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.response.notification.NotificationResponse;
import com.projecttaskmanager.backend.mapper.NotificationMapper;
import com.projecttaskmanager.backend.models.Notification;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.repositories.NotificationRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.EmailService;
import com.projecttaskmanager.backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    private User currentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Override
    public void send(UUID receiverId, String title, String content, String type, UUID referenceId, UUID actorId) {
        User receiver = userRepository.findById(receiverId).orElseThrow();

        if (actorId != null && actorId.equals(receiverId)) {
            return;
        }

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .title(title)
                        .content(content)
                        .type(type)
                        .referenceId(referenceId)
                        .receiver(receiver)
                        .isRead(false)
                        .createdAt(Instant.now())
                        .build()
        );

        NotificationResponse response = mapper.toResponse(notification);

        // Gửi realtime qua WebSocket
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/notifications",
                response
        );

        // Gửi email (bất đồng bộ)
        emailService.send(receiver.getEmail(), title, content);
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        return notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(currentUser().getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void markAsRead(UUID id) {
        Notification n = notificationRepository.findById(id).orElseThrow();
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Override
    public long countUnread() {
        return notificationRepository
                .countByReceiverIdAndIsReadFalse(currentUser().getId());
    }
}