package com.projecttaskmanager.backend.mapper;

import com.projecttaskmanager.backend.dto.response.activity.ActivityResponse;
import com.projecttaskmanager.backend.models.ActivityLog;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityResponse toResponse(ActivityLog log) {
        return ActivityResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .projectId(log.getProject().getId())
                .actorName(log.getActor().getFullName())
                .createdAt(log.getCreatedAt())
                .metadata(log.getMetadata())
                .build();
    }
}