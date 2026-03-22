package com.projecttaskmanager.backend.repositories;

import com.projecttaskmanager.backend.models.ActivityLog;
import com.projecttaskmanager.backend.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findByProjectOrderByCreatedAtDesc(Project project);
    List<ActivityLog> findByActorIdOrderByCreatedAtDesc(UUID actorId);
}