package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(UUID receiverId);
    long countByReceiverIdAndIsReadFalse(UUID receiverId);
}