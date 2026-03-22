package com.projecttaskmanager.backend.services;

import com.projecttaskmanager.backend.dto.response.activity.ActivityResponse;
import com.projecttaskmanager.backend.models.enums.ActivityAction;

import java.util.List;
import java.util.UUID;

public interface ActivityLogService {
    void log(ActivityAction action,
             String entityType,
             UUID entityId,
             UUID projectId,
             String metadata,
             UUID actorId);
    List<ActivityResponse> getProjectActivities(UUID projectId);
    List<ActivityResponse> getMyActivities();
}